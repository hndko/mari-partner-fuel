// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: get-my-entitlements
// Layanan Otoritatif Penentuan Hak Akses (Entitlement) & Kuota AI Pengguna
// Validasi berbasis Server-Side & PostgreSQL RPC (Zero-Client-Trust)
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

interface EntitlementResponse {
  user_id: string;
  is_pro: boolean;
  plan_code: string;
  plan_name: string;
  status: string;
  expires_at: string | null;
  auto_renew: boolean;
  max_vehicles: number | null;
  features: string[];
  ai_quota: {
    limit: number;
    used: number;
    remaining: number;
    resets_at: string;
  };
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

    // 1. Verifikasi JWT pengguna melalui user client
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

    // 2. Gunakan admin client untuk mengeksekusi perhitungan otoritatif di backend
    const adminClient = createClient(supabaseUrl, supabaseServiceKey || supabaseAnonKey);

    // Panggil RPC get_user_effective_entitlement
    const { data: rawEntitlement, error: entitlementError } = await adminClient.rpc(
      "get_user_effective_entitlement",
      { p_user_id: user.id }
    );

    if (entitlementError || !rawEntitlement) {
      // Fallback aman ke Free jika RPC belum siap
      const defaultFree: EntitlementResponse = {
        user_id: user.id,
        is_pro: false,
        plan_code: "free",
        plan_name: "Mari Free",
        status: "active",
        expires_at: null,
        auto_renew: false,
        max_vehicles: 2,
        features: ["manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"],
        ai_quota: {
          limit: 5,
          used: 0,
          remaining: 5,
          resets_at: getNextMonthResetIso(),
        },
      };
      return new Response(JSON.stringify(defaultFree), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // 3. Hitung pemakaian kuota AI bulan berjalan
    const currentMonth = getCurrentBillingMonth();
    const { data: usageData } = await adminClient
      .from("ai_usage_logs")
      .select("tokens_or_units")
      .eq("user_id", user.id)
      .eq("billing_month", currentMonth)
      .in("status", ["reserved", "confirmed"]);

    const totalUsed = (usageData ?? []).reduce((acc: number, row: any) => acc + (row.tokens_or_units || 1), 0);
    const quotaLimit = Number(rawEntitlement.ai_scan_quota ?? 5);
    const quotaRemaining = Math.max(0, quotaLimit - totalUsed);

    const response: EntitlementResponse = {
      user_id: user.id,
      is_pro: Boolean(rawEntitlement.is_pro),
      plan_code: String(rawEntitlement.plan_code || "free"),
      plan_name: String(rawEntitlement.plan_name || "Mari Free"),
      status: String(rawEntitlement.status || "active"),
      expires_at: rawEntitlement.expires_at || null,
      auto_renew: Boolean(rawEntitlement.auto_renew),
      max_vehicles: rawEntitlement.max_vehicles ? Number(rawEntitlement.max_vehicles) : null,
      features: Array.isArray(rawEntitlement.features) ? rawEntitlement.features : [],
      ai_quota: {
        limit: quotaLimit,
        used: totalUsed,
        remaining: quotaRemaining,
        resets_at: getNextMonthResetIso(),
      },
    };

    return new Response(JSON.stringify(response), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Terjadi kesalahan internal server" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});

function getCurrentBillingMonth(): string {
  const now = new Date();
  const year = now.getUTCFullYear();
  const month = String(now.getUTCMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

function getNextMonthResetIso(): string {
  const now = new Date();
  const nextMonth = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() + 1, 1, 0, 0, 0, 0));
  return nextMonth.toISOString();
}
