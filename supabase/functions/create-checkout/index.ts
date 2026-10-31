// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: create-checkout
// Membuat Order Pembayaran Berlisensi Resmi (Server-Authoritative Pricing)
// Mendukung Google Play Billing & QRISpy Gateway
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

interface CreateCheckoutRequest {
  plan_code: string;
  provider: "google_play" | "qrispy" | "midtrans" | "xendit" | "sandbox";
}

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

    // 1. Verifikasi Pengguna
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

    const payload: CreateCheckoutRequest = await req.json();
    if (!payload.plan_code || !payload.provider) {
      return new Response(
        JSON.stringify({ success: false, error: "Parameter plan_code dan provider wajib diisi" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const adminClient = createClient(supabaseUrl, supabaseServiceKey || supabaseAnonKey);

    // 2. Ambil Harga Resmi dari subscription_plans (Server-Authoritative)
    const { data: plan, error: planError } = await adminClient
      .from("subscription_plans")
      .select("*")
      .eq("code", payload.plan_code)
      .eq("is_active", true)
      .single();

    if (planError || !plan) {
      return new Response(
        JSON.stringify({ success: false, error: `Paket ${payload.plan_code} tidak ditemukan atau tidak aktif` }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 2.1 Evaluasi Harga Promo & Batas Pembelian (Purchase Limit Per User)
    let finalPriceIdr = plan.price_idr;
    let isPromoApplied = false;

    if (plan.is_promo_active && plan.promo_price_idr != null) {
      // Hitung riwayat transaksi promo user untuk paket ini
      const { count } = await adminClient
        .from("payment_transactions")
        .select("id", { count: "exact", head: true })
        .eq("user_id", user.id)
        .eq("status", "paid")
        .eq("is_promo", true)
        .or(`metadata->>plan_code.eq.${plan.code},product_id.eq.${plan.code}`);

      const userPromoCount = count ?? 0;
      const isEligible = plan.promo_max_per_user == null || plan.promo_max_per_user <= 0 || userPromoCount < plan.promo_max_per_user;

      if (isEligible) {
        finalPriceIdr = plan.promo_price_idr;
        isPromoApplied = true;
      }
    }

    if (finalPriceIdr <= 0) {
      return new Response(
        JSON.stringify({ success: false, error: "Paket gratis tidak memerlukan alur pembayaran" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 3. Generate Referensi Pembayaran Unik
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, "");
    const randomHex = crypto.randomUUID().slice(0, 8).toUpperCase();
    const paymentReference = `MPF-${today}-${randomHex}`;

    // Waktu kedaluwarsa order (default 15 menit untuk QRIS, 24 jam untuk Play Store)
    const isQris = payload.provider === "qrispy" || payload.provider === "midtrans";
    const expirationDurationMs = isQris ? 15 * 60 * 1000 : 24 * 60 * 60 * 1000;
    const expiresAt = new Date(Date.now() + expirationDurationMs).toISOString();

    const productId = "mari_pro";
    const basePlanId = plan.billing_period === "yearly" ? "pro-yearly" : "pro-monthly";

    let checkoutUrl: string | null = null;
    let qrisImageUrl: string | null = null;

    // 4. Integrasi Provider Spesifik
    if (payload.provider === "qrispy") {
      const qrispyApiKey = Deno.env.get("QRISPY_API_KEY");
      if (qrispyApiKey) {
        // Panggil endpoint resmi QRISpy: POST https://api.qrispy.id/api/payment/qris/generate
        try {
          const qrisResponse = await fetch("https://api.qrispy.id/api/payment/qris/generate", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "X-API-Token": qrispyApiKey,
            },
            body: JSON.stringify({
              amount: plan.price_idr,
              partner_reference_no: paymentReference,
              customer_id: user.id,
              validity_period: 900, // 15 menit
            }),
          });
          const qrisData = await qrisResponse.json();
          if (qrisData?.data) {
            checkoutUrl = qrisData.data.checkout_url ?? null;
            qrisImageUrl = qrisData.data.qr_image_url ?? qrisData.data.qr_string ?? null;
          }
        } catch (_e) {
          // Fallback ke simulasi sandbox jika offline/jaringan terkendala
        }
      }

      if (!qrisImageUrl) {
        // Safe Sandbox QRIS payload untuk pengujian lingkungan development
        qrisImageUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=00020101021226580016ID.CO.MARIPARTNER0118${paymentReference}520454115303360540${finalPriceIdr}5802ID5917MARI_PARTNER_FUEL6007JAKARTA`;
      }
    }

    // 5. Simpan Record ke payment_transactions
    const { error: insertError } = await adminClient.from("payment_transactions").insert({
      user_id: user.id,
      provider: payload.provider,
      payment_reference: paymentReference,
      amount_idr: finalPriceIdr,
      currency: "IDR",
      status: "pending",
      is_promo: isPromoApplied,
      product_id: productId,
      base_plan_id: basePlanId,
      billing_period: plan.billing_period,
      checkout_url: checkoutUrl,
      qris_image_url: qrisImageUrl,
      expires_at: expiresAt,
      metadata: {
        plan_code: plan.code,
        plan_name: plan.name,
        user_email: user.email,
        regular_price_idr: plan.price_idr,
        is_promo: isPromoApplied,
        promo_label: isPromoApplied ? plan.promo_label : null
      },
    });

    if (insertError) {
      return new Response(
        JSON.stringify({ success: false, error: "Gagal mencatat order pembayaran: " + insertError.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({
        success: true,
        payment_reference: paymentReference,
        provider: payload.provider,
        amount_idr: finalPriceIdr,
        is_promo: isPromoApplied,
        currency: "IDR",
        plan_code: plan.code,
        plan_name: plan.name,
        billing_period: plan.billing_period,
        product_id: productId,
        base_plan_id: basePlanId,
        checkout_url: checkoutUrl,
        qris_image_url: qrisImageUrl,
        expires_at: expiresAt,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (_err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Terjadi kesalahan internal server saat membuat order pembayaran" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
