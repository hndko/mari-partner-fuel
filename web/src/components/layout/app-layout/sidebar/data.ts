import {
  LayoutGridIcon,
  TableIcon,
  UserCircleIcon,
  BoxIcon,
  PieChartIcon,
  PageIcon,
} from '@/icons/layout'
import type { NavItem } from './types'

export const navItems: NavItem[] = [
  {
    icon: LayoutGridIcon,
    name: 'Ringkasan SaaS',
    path: '/admin/dashboard',
  },
  {
    icon: BoxIcon,
    name: 'Katalog Paket & Harga',
    path: '/admin/plans',
  },
  {
    icon: UserCircleIcon,
    name: 'Manajemen Pengguna',
    path: '/admin/users',
  },
  {
    icon: TableIcon,
    name: 'Audit Transaksi Masuk',
    path: '/admin/transactions',
  },
  {
    icon: PieChartIcon,
    name: 'Mutasi Gateway QRISpy',
    path: '/admin/qrispy',
  },
]

export const othersItems: NavItem[] = [
  {
    icon: PageIcon,
    name: 'Lihat Website Publik',
    path: '/',
  },
]
