import { createClient } from '@supabase/supabase-js'

const supabaseUrl =
  (import.meta.env.SUPABASE_URL as string) ||
  (import.meta.env.VITE_SUPABASE_URL as string) ||
  'https://zcezyhgrblksfuxmszyz.supabase.co'

const supabaseAnonKey =
  (import.meta.env.SUPABASE_ANON_KEY as string) ||
  (import.meta.env.VITE_SUPABASE_ANON_KEY as string) ||
  ''

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true,
  },
})

export interface OwnerProfile {
  id: string
  email: string
  displayName: string
  role: string
  isOwner: boolean
}

export interface QrispyMerchantBalance {
  merchantId: string
  merchantName: string
  balanceIdr: number
  currency: string
}

export interface QrispyMerchantTransaction {
  qrisId: string
  amountIdr: number
  status: string
  qrisImageUrl: string | null
  createdAt: string
  expiredAt: string
  paymentReference: string
  generatedVia: string
}

export interface QrispyMerchantData {
  balance: QrispyMerchantBalance
  transactions: QrispyMerchantTransaction[]
}

export async function getCurrentOwner(): Promise<OwnerProfile | null> {
  const { data: { session } } = await supabase.auth.getSession()
  if (!session?.user) return null

  const email = session.user.email ?? ''
  const isOwnerEmail = email.toLowerCase() === 'kyuoko13@gmail.com'

  const { data: profile } = await supabase
    .from('profiles')
    .select('role, display_name')
    .eq('id', session.user.id)
    .single()

  const role = profile?.role ?? (isOwnerEmail ? 'owner' : 'user')
  const isOwner = isOwnerEmail || role === 'owner' || role === 'admin'

  return {
    id: session.user.id,
    email,
    displayName: profile?.display_name || session.user.user_metadata?.full_name || 'Owner',
    role,
    isOwner,
  }
}

export async function fetchQrispyMerchantData(): Promise<QrispyMerchantData> {
  const { data: { session } } = await supabase.auth.getSession()
  const token = session?.access_token || supabaseAnonKey

  const response = await fetch(`${supabaseUrl}/functions/v1/get-qrispy-merchant-data`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      apikey: supabaseAnonKey,
    },
  })

  if (!response.ok) {
    const errText = await response.text()
    throw new Error(`Gagal mengambil data QRISpy: ${errText}`)
  }

  const json = await response.json()
  const balanceRaw = json.data?.balance || {}
  const transactionsRaw = json.data?.transactions || []

  return {
    balance: {
      merchantId: balanceRaw.merchant_id || '',
      merchantName: balanceRaw.merchant_name || 'Mari Partner Fuel',
      balanceIdr: Number(balanceRaw.balance || 0),
      currency: balanceRaw.currency || 'IDR',
    },
    transactions: transactionsRaw.map((tx: any) => ({
      qrisId: tx.qris_id || '',
      amountIdr: Number(tx.amount || 0),
      status: tx.status || 'pending',
      qrisImageUrl: tx.qris_image_url || null,
      createdAt: tx.created_at || '',
      expiredAt: tx.expired_at || '',
      paymentReference: tx.payment_reference || '',
      generatedVia: tx.generated_via || 'api',
    })),
  }
}
