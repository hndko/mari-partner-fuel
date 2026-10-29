// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: get-payment-status
// Memeriksa Status Pembayaran Terkini & Sinkronisasi Hak Akses Pro
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
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
        JSON.stringify({ success: false, error: "Sesi tidak valid" }),
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

    // Ambil data transaksi milik user
    const { data: tx, error: txError } = await adminClient
      .from("payment_transactions")
      .select("*, subscriptions(*)")
      .eq("payment_reference", paymentRef)
      .eq("user_id", user.id)
      .single();

    if (txError || !tx) {
      return new Response(
        JSON.stringify({ success: false, error: "Transaksi pembayaran tidak ditemukan" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Ambil entitlement user terbaru
    const { data: entitlement } = await adminClient.rpc(
      "get_user_effective_entitlement",
      { p_user_id: user.id }
    );

    const isPaid = tx.status === "paid";
    const isPro = Boolean(entitlement?.is_pro);

    return new Response(
      JSON.stringify({
        success: true,
        payment_reference: tx.payment_reference,
        provider: tx.provider,
        amount_idr: tx.amount_idr,
        status: tx.status,
        is_paid: isPaid,
        is_pro: isPro,
        paid_at: tx.paid_at,
        expires_at: tx.expires_at,
        subscription_expires_at: entitlement?.expires_at ?? null,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Terjadi kesalahan internal: " + err.message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
