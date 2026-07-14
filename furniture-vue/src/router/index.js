import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Standalone pages (no layout)
    {
      path: "/login",
      name: "Login",
      component: () => import("@/views/LoginView.vue"),
      meta: { public: true },
    },
    {
      path: "/register",
      name: "Register",
      component: () => import("@/views/RegisterView.vue"),
      meta: { public: true },
    },
    {
      path: "/forgot-password",
      name: "ForgotPassword",
      component: () => import("@/views/ForgotPasswordView.vue"),
      meta: { public: true },
    },

    // Main layout — all front-facing pages
    {
      path: "/",
      component: () => import("@/layouts/DefaultLayout.vue"),
      children: [
        {
          path: "",
          name: "Home",
          component: () => import("@/views/HomeView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "type/:id",
          name: "TypeDetail",
          component: () => import("@/views/TypeDetailView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "furniture/detail/:id",
          name: "FurnitureDetail",
          component: () => import("@/views/FurnitureDetailView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "cart",
          name: "Cart",
          component: () => import("@/views/CartView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "about",
          name: "About",
          component: () => import("@/views/AboutView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "user",
          children: [
            {
              path: "profile",
              name: "Profile",
              component: () => import("@/views/ProfileView.vue"),
              meta: { requiresAuth: true },
            },
            {
              path: "orders",
              name: "UserOrders",
              component: () => import("@/views/UserOrdersView.vue"),
              meta: { requiresAuth: true },
            },
            {
              path: "favorites",
              name: "UserFavorites",
              component: () => import("@/views/UserFavoritesView.vue"),
              meta: { requiresAuth: true },
            },
            {
              path: "addresses",
              name: "UserAddresses",
              component: () => import("@/views/AddressView.vue"),
              meta: { requiresAuth: true },
            },
          ],
        },
        {
          path: "order/pay/:id",
          name: "OrderPay",
          component: () => import("@/views/OrderPayView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "notification",
          name: "Notification",
          component: () => import("@/views/NotificationView.vue"),
          meta: { requiresAuth: true },
        },
      ],
    },

    // Admin
    {
      path: "/admin",
      component: () => import("@/views/admin/AdminLayout.vue"),
      meta: { requiresAdmin: true },
      children: [
        { path: "", redirect: "/admin/dashboard" },
        {
          path: "dashboard",
          component: () => import("@/views/admin/AdminDashboard.vue"),
        },
        {
          path: "users",
          component: () => import("@/views/admin/UserManage.vue"),
        },
        {
          path: "furniture",
          component: () => import("@/views/admin/FurnitureManage.vue"),
        },
        {
          path: "orders",
          component: () => import("@/views/admin/OrderManage.vue"),
        },
        {
          path: "furniture_type",
          component: () => import("@/views/admin/FurnitureTypeManage.vue"),
        },
        {
          path: "notification",
          component: () => import("@/views/admin/NotificationManage.vue"),
        },
        {
          path: "comments",
          component: () => import("@/views/admin/CommentManage.vue"),
        },
        {
          path: "site-content",
          component: () => import("@/views/admin/SiteContentManage.vue"),
        },
      ],
    },

    // 404
    {
      path: "/:pathMatch(.*)*",
      name: "NotFound",
      component: () => import("@/views/NotFoundView.vue"),
      meta: { public: true },
    },
  ],
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  const isLoggedIn = !!token;
  let userRole = null;
  const userInfo = localStorage.getItem("userInfo");
  if (userInfo) {
    try {
      userRole = JSON.parse(userInfo).isAdmin;
    } catch (e) {}
  }
  if (to.meta.requiresAuth && !isLoggedIn) {
    next("/login");
  } else if (to.meta.requiresAdmin && userRole !== 1) {
    next("/");
  } else if (to.path === "/login" && isLoggedIn) {
    next("/");
  } else {
    next();
  }
});

export default router;
