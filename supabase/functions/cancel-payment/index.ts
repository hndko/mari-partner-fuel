// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: cancel-payment
// Pembatalan Manual Transaksi QRIS Sebelum Batas Waktu 15 Menit Berakhir
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ success: false, error: "Header Authorization tidak ditemukan" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

    const userClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: { user }, error: userError } = await userClient.auth.getUser();
    if (userError || !user) {
      return new Response(
        JSON.stringify({ success: false, error: "Sesi tidak valid atau telah kedaluwarsa" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const payload = await req.json();
    const paymentRef = payload.payment_reference;
    if (!paymentRef) {
      return new Response(
        JSON.stringify({ success: false, error: "payment_reference wajib diisi" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const adminClient = createClient(supabaseUrl, supabaseServiceKey || supabaseAnonKey);

    // Ambil data transaksi milik pengguna
    const { data: tx, error: txError } = await adminClient
      .from("payment_transactions")
      .select("*")
      .eq("payment_reference", paymentRef)
      .eq("user_id", user.id)
      .maybeSingle();

    if (txError || !tx) {
      return new Response(
        JSON.stringify({ success: false, error: "Transaksi pembayaran tidak ditemukan" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (tx.status === "paid") {
      return new Response(
        JSON.stringify({ success: false, error: "Transaksi yang sudah dibayar tidak dapat dibatalkan" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (tx.status === "cancelled") {
      return new Response(
        JSON.stringify({ success: true, message: "Transaksi sudah dalam status dibatalkan" }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Jika provider adalah QRISpy dan memiliki ID transaksi qris
    if (tx.provider === "qrispy" && tx.provider_payment_id) {
      const qrispyApiToken = Deno.env.get("QRISPY_API_TOKEN") ?? Deno.env.get("QRISPY_API_KEY");
      if (qrispyApiToken) {
        try {
          const cancelRes = await fetch(`https://api.qrispy.id/api/payment/qris/${tx.provider_payment_id}/cancel`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "X-API-Token": qrispyApiToken,
            },
          });
          const cancelJson = await cancelRes.json();
          // Jika 400/404 atau pesan sukses
        } catch (_e) {
          // Abaikan kendala jaringan ke gateway, tetap update status lokal
        }
      }
    }

    // Update status transaksi menjadi 'cancelled'
    const { error: updateError } = await adminClient
      .from("payment_transactions")
      .update({
        status: "cancelled",
        updated_at: new Date().toISOString(),
      })
      .eq("id", tx.id);

    if (updateError) {
      return new Response(
        JSON.stringify({ success: false, error: "Gagal memperbarui status transaksi: " + updateError.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: "Transaksi pembayaran QRIS berhasil dibatalkan.",
        payment_reference: paymentRef,
        status: "cancelled",
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (_err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Terjadi kesalahan internal server saat membatalkan transaksi" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
