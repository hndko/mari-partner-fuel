// =============================================================================
// MARI PARTNER FUEL — SUPABASE EDGE FUNCTION: extract-fuel-receipt
// Memproses ekstraksi data struk BBM menggunakan Cloud AI (Gemini 1.5 Flash)
// dengan verifikasi JWT pengguna dan prinsip Zero-Secret di sisi client Android.
// =============================================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

interface ExtractionRequest {
  storage_path?: string;
  raw_ocr_text?: string;
  provider?: string;
}

interface ExtractedReceiptFields {
  transaction_date: string | null;
  transaction_time: string | null;
  total_amount_idr: number | null;
  volume_liters: number | null;
  price_per_liter_idr: number | null;
  fuel_type: string | null;
  station_name: string | null;
  receipt_number: string | null;
}

interface ExtractionResponse {
  success: boolean;
  provider: string;
  fields: ExtractedReceiptFields | null;
  warnings: string[];
  needs_review: boolean;
  error?: string;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const startTime = Date.now();

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ success: false, error: "Missing Authorization header" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

    // Klien untuk memverifikasi JWT pengguna aktif
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

    const payload: ExtractionRequest = await req.json();

    if (!payload.storage_path && !payload.raw_ocr_text) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "Harus menyertakan storage_path atau raw_ocr_text",
        }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Validasi isolasi file: pengguna hanya boleh meminta ekstraksi pada file miliknya sendiri
    if (payload.storage_path && !payload.storage_path.startsWith(`${user.id}/`)) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "Akses ditolak: File struk bukan milik pengguna ini",
        }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const geminiApiKey = Deno.env.get("GEMINI_API_KEY");

    // Jika secret Gemini belum dikonfigurasi di server
    if (!geminiApiKey) {
      return new Response(
        JSON.stringify({
          success: false,
          provider: "gemini_cloud",
          fields: null,
          warnings: ["Secret GEMINI_API_KEY belum dikonfigurasi di Supabase Edge Function Secrets."],
          needs_review: true,
          error: "Layanan Cloud AI belum dikonfigurasi oleh administrator.",
        } satisfies ExtractionResponse),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Panggil Gemini 1.5 Flash via REST API resmi
    const prompt = `Anda adalah asisten AI ahli ekstraksi struk SPBU Indonesia (Pertamina, Shell, BP, Vivo, dll).
Tugas Anda adalah membaca teks struk atau detail struk berikut dan mengekstrak data transaksi bahan bakar ke dalam format JSON murni.

Aturan Penting:
1. total_amount_idr: angka integer Rupiah (misal 50000). Cari label TOTAL/JUMLAH/BAYAR, jangan tertukar dengan uang tunai (CASH) atau kembalian (CHANGE).
2. volume_liters: angka desimal float (misal 3.850). Cari label LITER/VOL/QTY.
3. price_per_liter_idr: harga satuan per liter (misal 10000 atau 12950).
4. fuel_type: nama BBM standar (Pertalite, Pertamax, Pertamax Turbo, Solar, Dexlite, Pertamina Dex, Shell Super, dll).
5. station_name: nama atau nomor kode SPBU (misal: "SPBU Pertamina 31.12345", "Shell Gatot Subroto").
6. transaction_date: format YYYY-MM-DD jika ditemukan (misal: "2026-10-29"). Jika tidak yakin, isi null.
7. transaction_time: format HH:MM jika ditemukan (misal: "14:30"). Jika tidak yakin, isi null.
8. receipt_number: nomor struk atau nomor referensi transaksi jika ada.
9. Jika nilai tidak ditemukan atau meragukan, WAJIB isi dengan null. Jangan mengarang angka nol atau menebak.

Input Teks Struk:
${payload.raw_ocr_text ?? "Teks tidak tersedia, hanya path foto."}

Keluarkan HANYA JSON valid dengan struktur:
{
  "transaction_date": string or null,
  "transaction_time": string or null,
  "total_amount_idr": number or null,
  "volume_liters": number or null,
  "price_per_liter_idr": number or null,
  "fuel_type": string or null,
  "station_name": string or null,
  "receipt_number": string or null,
  "warnings": [string]
}`;

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiApiKey}`;

    const geminiResponse = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.1,
          responseMimeType: "application/json",
        },
      }),
    });

    if (!geminiResponse.ok) {
      const errText = await geminiResponse.text();
      return new Response(
        JSON.stringify({
          success: false,
          provider: "gemini_cloud",
          fields: null,
          warnings: ["Provider AI mengembalikan status error."],
          needs_review: true,
          error: "Gagal memproses data melalui provider AI eksternal.",
        } satisfies ExtractionResponse),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const geminiData = await geminiResponse.json();
    const rawAiText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text ?? "{}";

    let parsedResult: any = {};
    try {
      parsedResult = JSON.parse(rawAiText);
    } catch {
      parsedResult = {};
    }

    const fields: ExtractedReceiptFields = {
      transaction_date: parsedResult.transaction_date ?? null,
      transaction_time: parsedResult.transaction_time ?? null,
      total_amount_idr: typeof parsedResult.total_amount_idr === "number" ? Math.round(parsedResult.total_amount_idr) : null,
      volume_liters: typeof parsedResult.volume_liters === "number" ? parsedResult.volume_liters : null,
      price_per_liter_idr: typeof parsedResult.price_per_liter_idr === "number" ? parsedResult.price_per_liter_idr : null,
      fuel_type: typeof parsedResult.fuel_type === "string" ? parsedResult.fuel_type : null,
      station_name: typeof parsedResult.station_name === "string" ? parsedResult.station_name : null,
      receipt_number: typeof parsedResult.receipt_number === "string" ? parsedResult.receipt_number : null,
    };

    const warnings: string[] = Array.isArray(parsedResult.warnings) ? parsedResult.warnings : [];

    // Validasi selisih perkalian volume x harga
    if (fields.total_amount_idr && fields.volume_liters && fields.price_per_liter_idr) {
      const expected = Math.round(fields.volume_liters * fields.price_per_liter_idr);
      const diff = Math.abs(fields.total_amount_idr - expected);
      if (diff > fields.total_amount_idr * 0.05 && diff > 1000) {
        warnings.push(`Perhitungan volume x harga (${expected}) memiliki selisih Rp ${diff} dengan total.`);
      }
    }

    const durationMs = Date.now() - startTime;

    // Catat audit pekerjaan scan ke tabel receipt_scan_jobs (menggunakan admin client agar andal)
    if (supabaseServiceKey) {
      try {
        const adminClient = createClient(supabaseUrl, supabaseServiceKey);
        await adminClient.from("receipt_scan_jobs").insert({
          user_id: user.id,
          provider: "gemini_cloud",
          status: "completed",
          raw_extracted_text: payload.raw_ocr_text ?? null,
          structured_result: fields,
          duration_ms: durationMs,
        });
      } catch (_e) {
        // Abaikan kegagalan logging agar tidak membatalkan respons ke pengguna
      }
    }

    const response: ExtractionResponse = {
      success: true,
      provider: "gemini_cloud",
      fields,
      warnings,
      needs_review: true,
    };

    return new Response(JSON.stringify(response), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: any) {
    return new Response(
      JSON.stringify({
        success: false,
        provider: "unknown",
        fields: null,
        warnings: [],
        needs_review: true,
        error: "Terjadi kesalahan internal pada server backend.",
      } satisfies ExtractionResponse),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
