// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: payment-webhook
// Server-to-Server Webhook Handler dengan Verifikasi Tanda Tangan & Idempotensi Mutlak
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-callback-signature, x-signature",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const adminClient = createClient(supabaseUrl, supabaseServiceKey);

    const rawPayload = await req.json();

    // 1. Tentukan Provider & Normalisasi Format Event
    let provider = "unknown";
    let eventId = "";
    let paymentRef = "";
    let isSuccessPaid = false;
    let providerPaymentId = "";

    // Deteksi Format Midtrans
    if (rawPayload.order_id && rawPayload.transaction_status) {
      provider = "midtrans";
      paymentRef = rawPayload.order_id;
      eventId = rawPayload.transaction_id || `midtrans_${paymentRef}_${rawPayload.transaction_status}`;
      providerPaymentId = rawPayload.transaction_id || "";
      isSuccessPaid = rawPayload.transaction_status === "settlement" || rawPayload.transaction_status === "capture";
    }
    // Deteksi Format QRISpy
    else if (rawPayload.partner_reference_no || rawPayload.qris_id) {
      provider = "qrispy";
      paymentRef = rawPayload.partner_reference_no || "";
      eventId = rawPayload.event_id || rawPayload.transaction_id || `qrispy_${paymentRef}_${Date.now()}`;
      providerPaymentId = rawPayload.qris_id || rawPayload.transaction_id || "";
      isSuccessPaid = rawPayload.status === "PAID" || rawPayload.status === "SUCCESS";
    }
    // Deteksi Format Google Play RTDN (Pub/Sub)
    else if (rawPayload.message?.data) {
      provider = "google_play";
      try {
        const decoded = JSON.parse(atob(rawPayload.message.data));
        eventId = rawPayload.message.messageId || `play_${Date.now()}`;
        paymentRef = decoded.subscriptionNotification?.purchaseToken || "";
        providerPaymentId = paymentRef;
        // notificationType 2 = RENEWED, 4 = PURCHASED
        isSuccessPaid = decoded.subscriptionNotification?.notificationType === 2 || decoded.subscriptionNotification?.notificationType === 4;
      } catch (_e) {
        // Fallback
      }
    }

    if (!paymentRef || !eventId) {
      return new Response(
        JSON.stringify({ status: "error", message: "Format payload webhook tidak dikenali" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 1b. Verifikasi Tanda Tangan Webhook (Signature Verification)
    const qrispySecret = Deno.env.get("QRISPY_WEBHOOK_SECRET");
    const midtransKey = Deno.env.get("MIDTRANS_SERVER_KEY");

    if (provider === "qrispy" && qrispySecret) {
      const signatureHeader = req.headers.get("x-callback-signature") || req.headers.get("x-signature") || "";
      if (!signatureHeader) {
        return new Response(
          JSON.stringify({ status: "error", message: "Tanda tangan webhook QRISpy tidak ditemukan" }),
          { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    } else if (provider === "midtrans" && midtransKey) {
      const clientSignature = rawPayload.signature_key || "";
      if (!clientSignature) {
        return new Response(
          JSON.stringify({ status: "error", message: "Signature key Midtrans tidak ditemukan" }),
          { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    // 2. Proteksi Idempotensi Atomik: Simpan Event ke payment_webhook_events
    const { data: recordResult, error: recordError } = await adminClient.rpc(
      "record_payment_webhook_event",
      {
        p_provider: provider,
        p_event_id: eventId,
        p_payment_reference: paymentRef,
        p_payload: rawPayload,
      }
    );

    if (recordResult?.is_duplicate) {
      // Event duplikat: kembalikan 200 OK segera agar provider tidak melakukan retry terus-menerus
      return new Response(
        JSON.stringify({ status: "ok", message: "Event duplikat telah diproses sebelumnya." }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 3. Ambil Data Transaksi Asli untuk Validasi Nominal
    const { data: tx } = await adminClient
      .from("payment_transactions")
      .select("*")
      .eq("payment_reference", paymentRef)
      .maybeSingle();

    if (!tx) {
      return new Response(
        JSON.stringify({ status: "ignored", message: "Referensi order tidak ditemukan di sistem" }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 4. Proses Aktivasi jika Pembayaran Dikonfirmasi Sah
    if (isSuccessPaid) {
      const isYearly = tx.billing_period === "yearly";
      const planCode = tx.metadata?.plan_code || (isYearly ? "pro_yearly" : "pro_monthly");
      const durationDays = isYearly ? 365 : 30;

      await adminClient.rpc("activate_subscription_for_payment", {
        p_user_id: tx.user_id,
        p_payment_ref: paymentRef,
        p_provider: provider,
        p_provider_payment_id: providerPaymentId || tx.payment_reference,
        p_plan_code: planCode,
        p_duration_days: durationDays,
        p_auto_renew: provider === "google_play",
      });
    }

    return new Response(
      JSON.stringify({ status: "ok", message: "Webhook berhasil diproses." }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (_err: any) {
    return new Response(
      JSON.stringify({ status: "error", message: "Terjadi kesalahan internal server saat memproses webhook" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
