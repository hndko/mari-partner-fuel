import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentOwner } from '@/services/supabase'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { left: 0, top: 0 }
  },
  routes: [
    {
      path: '/',
      name: 'LandingPage',
      component: () => import('../views/LandingPageView.vue'),
      meta: {
        title: 'Mari Partner Fuel - AI Expense Tracker for Android',
      },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        title: 'Login SaaS Owner - Mari Partner Fuel',
      },
    },
    {
      path: '/admin',
      component: () => import('../components/layout/AdminLayout.vue'),
      meta: { requiresOwner: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard',
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('../views/admin/DashboardOverviewView.vue'),
          meta: {
            title: 'Ringkasan Eksekutif SaaS - Mari Partner Fuel',
          },
        },
        {
          path: 'plans',
          name: 'AdminPlans',
          component: () => import('../views/admin/PlansManagementView.vue'),
          meta: {
            title: 'Katalog Paket & Harga - Mari Partner Fuel',
          },
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('../views/admin/UsersManagementView.vue'),
          meta: {
            title: 'Manajemen Pengguna - Mari Partner Fuel',
          },
        },
        {
          path: 'transactions',
          name: 'AdminTransactions',
          component: () => import('../views/admin/TransactionsAuditView.vue'),
          meta: {
            title: 'Audit Mutasi Transaksi - Mari Partner Fuel',
          },
        },
        {
          path: 'qrispy',
          name: 'AdminQrispy',
          component: () => import('../views/admin/QrispyGatewayLedgerView.vue'),
          meta: {
            title: 'Mutasi Gateway QRISpy - Mari Partner Fuel',
          },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title as string
  }

  const isProtectedAdmin = to.matched.some((record) => record.meta.requiresOwner)

  if (isProtectedAdmin) {
    const owner = await getCurrentOwner()
    if (!owner?.isOwner) {
      return next('/login')
    }
  }

  if (to.path === '/login') {
    const owner = await getCurrentOwner()
    if (owner?.isOwner) {
      return next('/admin/dashboard')
    }
  }

  next()
})

export default router
