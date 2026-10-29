// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: sync-subscription
// Sinkronisasi Status Langganan Pro & Rekonsiliasi Entitlement Pengguna
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

    // Dapatkan hak akses terkini langsung dari PostgreSQL
    const { data: entitlement, error: entError } = await adminClient.rpc(
      "get_user_effective_entitlement",
      { p_user_id: user.id }
    );

    if (entError || !entitlement) {
      return new Response(
        JSON.stringify({ success: false, error: "Gagal menyinkronkan entitlement" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({
        success: true,
        user_id: user.id,
        is_pro: entitlement.is_pro,
        plan_code: entitlement.plan_code,
        plan_name: entitlement.plan_name,
        status: entitlement.status,
        expires_at: entitlement.expires_at,
        auto_renew: entitlement.auto_renew,
        max_vehicles: entitlement.max_vehicles,
        features: entitlement.features,
        synced_at: new Date().toISOString(),
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ success: false, error: "Internal error: " + err.message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
