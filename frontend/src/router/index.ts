import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import PhotoGallery from '@/views/PhotoGallery.vue'
import PhotoView from '@/views/PhotoView.vue'
import UploadFiles from '@/views/UploadFiles.vue'
import EditPhoto from '@/views/EditPhoto.vue'
import Login from '@/views/Login.vue'
import Signup from '@/views/Signup.vue'
import Profile from '@/views/Profile.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/photos'
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { requiresGuest: true }
    },
    {
      path: '/signup',
      name: 'signup',
      component: Signup,
      meta: { requiresGuest: true }
    },
    {
      path: '/photos',
      name: 'photos',
      component: PhotoGallery,
      meta: { requiresAuth: true }
    },
    {
      path: '/galleries',
      name: 'galleries',
      component: () => import('@/views/Galleries.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/gallery/:id',
      name: 'gallery-detail',
      component: () => import('@/views/GalleryDetail.vue'),
      props: true,
      meta: { requiresAuth: true }
    },
    {
      path: '/upload',
      name: 'upload',
      component: UploadFiles,
      meta: { requiresAuth: true }
    },
    {
      path: '/edit/:id',
      name: 'edit',
      component: EditPhoto,
      props: true,
      meta: { requiresAuth: true }
    },
    {
      path: '/photo/:id',
      name: 'photo',
      component: PhotoView,
      props: true,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { requiresAuth: true }
    }
  ]
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  // Routes that require authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  // Routes that require guest (not authenticated)
  if (to.meta.requiresGuest && isAuthenticated) {
    next('/photos')
    return
  }

  next()
})

export default router
