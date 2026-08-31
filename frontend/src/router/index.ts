import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, getStorage } from '@/utils/storage'
import type { UserInfo } from '@/types'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { keepAlive: true }
  },
  {
    path: '/category/:id?',
    name: 'Category',
    component: () => import('@/views/CategoryView.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetailView.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/CartView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guest: true }
  },
  {
    path: '/pay',
    name: 'Pay',
    component: () => import('@/views/PayView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/UserCenterView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'CouponCenter',
    component: () => import('@/views/CouponCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/NotificationListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/size-guide',
      name: 'SizeGuide',
      component: () => import('@/views/SizeGuideView.vue'),
    },
    {
      path: '/brand',
      name: 'BrandStory',
      component: () => import('@/views/BrandStoryView.vue'),
    },
    {
      path: '/designers',
      name: 'DesignerTeam',
      component: () => import('@/views/DesignerTeamView.vue'),
    },
    {
      path: '/shipping',
      name: 'Shipping',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'shipping' },
    },
    {
      path: '/returns',
      name: 'Returns',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'returns' },
    },
    {
      path: '/payment',
      name: 'Payment',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'payment' },
    },
    {
      path: '/stores',
      name: 'Stores',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'stores' },
    },
    {
      path: '/careers',
      name: 'Careers',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'careers' },
    },
    {
      path: '/faq',
      name: 'FAQ',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'faq' },
    },
    {
      path: '/contact',
      name: 'Contact',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'contact' },
    },
    {
      path: '/privacy',
      name: 'Privacy',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'privacy' },
    },
    {
      path: '/terms',
      name: 'Terms',
      component: () => import('@/views/InfoPageView.vue'),
      meta: { page: 'terms' },
    },
    {
      path: '/admin',
      redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/AdminProducts.vue'),
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/AdminOrders.vue'),
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
      },
      {
        path: 'refunds',
        name: 'AdminRefunds',
        component: () => import('@/views/admin/AdminRefunds.vue'),
      },
      {
        path: 'coupons',
        name: 'AdminCoupons',
        component: () => import('@/views/admin/AdminCoupons.vue'),
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('@/views/admin/AdminReviews.vue'),
      },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  const userInfo = getStorage<UserInfo>('userInfo')
  const isAdmin = userInfo?.role === 'ADMIN'




  if (to.meta.requiresAuth && !token) {
    next('/login?redirect=' + to.fullPath)
  } else if (to.meta.guest && token) {
    next('/')
  } else if (to.meta.requiresAdmin) {
    if (!isAdmin) {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
