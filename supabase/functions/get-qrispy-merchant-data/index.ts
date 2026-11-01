// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: get-qrispy-merchant-data
// Mengambil Saldo Merchant & Mutasi Transaksi Resmi dari QRISpy Gateway API
// Khusus diakses oleh Owner / Admin SaaS Mari Partner Fuel
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

    const adminClient = createClient(supabaseUrl, supabaseServiceKey || supabaseAnonKey);

    // 1. Verifikasi Hak Akses Owner / Admin
    const { data: profile } = await adminClient
      .from("profiles")
      .select("role")
      .eq("id", user.id)
      .maybeSingle();

    const isOwner = user.email?.toLowerCase() === "kyuoko13@gmail.com" ||
      profile?.role === "owner" ||
      profile?.role === "admin";

    if (!isOwner) {
      return new Response(
        JSON.stringify({ success: false, error: "Akses ditolak: Anda bukan Owner/Admin" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 2. Ambil Kredensial QRISpy dari Secret Server
    const qrispyToken = Deno.env.get("QRISPY_API_TOKEN") ?? Deno.env.get("QRISPY_API_KEY");
    if (!qrispyToken) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "QRISPY_API_TOKEN belum dikonfigurasi di Supabase Secrets.",
          data: {
            balance: {
              merchant_id: "SANDBOX-MOCK",
              merchant_name: "Mari Partner Fuel (Sandbox)",
              balance: 0,
              currency: "IDR"
            },
            transactions: []
          }
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 3. Panggil Endpoint Saldo QRISpy: GET https://api.qrispy.id/api/payment/balance
    let balanceData = {
      merchant_id: "",
      merchant_name: "Mari Partner Fuel",
      balance: 0,
      currency: "IDR"
    };

    try {
      const balanceRes = await fetch("https://api.qrispy.id/api/payment/balance", {
        method: "GET",
        headers: {
          "X-API-Token": qrispyToken,
        },
      });
      const balanceJson = await balanceRes.json();
      if (balanceJson?.data) {
        balanceData = {
          merchant_id: balanceJson.data.merchant_id ?? "",
          merchant_name: balanceJson.data.merchant_name ?? "Mari Partner Fuel",
          balance: Number(balanceJson.data.balance ?? 0),
          currency: balanceJson.data.currency ?? "IDR",
        };
      }
    } catch (_e) {
      // Abaikan jika network issue
    }

    // 4. Panggil Endpoint Mutasi Transaksi QRISpy: GET https://api.qrispy.id/api/payment/transactions?limit=20
    let transactionsList: any[] = [];
    try {
      const txRes = await fetch("https://api.qrispy.id/api/payment/transactions?limit=20", {
        method: "GET",
        headers: {
          "X-API-Token": qrispyToken,
        },
      });
      const txJson = await txRes.json();
      if (Array.isArray(txJson?.data)) {
        transactionsList = txJson.data.map((item: any) => ({
          qris_id: item.qris_id ?? "",
          amount: Number(item.amount ?? item.received_amount ?? 0),
          status: (item.status ?? "pending").toLowerCase(),
          qris_image_url: item.qris_image_url ?? null,
          created_at: item.created_at ?? "",
          expired_at: item.expired_at ?? "",
          payment_reference: item.payment_reference ?? "",
          generated_via: item.generated_via ?? "api"
        }));
      }
    } catch (_e) {
      // Abaikan jika network issue
    }

    return new Response(
      JSON.stringify({
        success: true,
        data: {
          balance: balanceData,
          transactions: transactionsList,
        }
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (_err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Terjadi kesalahan internal server saat mengambil data merchant QRISpy" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
