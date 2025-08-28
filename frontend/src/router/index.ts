import { createRouter, createWebHistory } from 'vue-router'
import PhotoGallery from '@/views/PhotoGallery.vue'
import UploadFiles from '@/views/UploadFiles.vue'
import EditPhoto from '@/views/EditPhoto.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'gallery',
      component: PhotoGallery
    },
    {
      path: '/upload',
      name: 'upload',
      component: UploadFiles
    },
    {
      path: '/edit/:id',
      name: 'edit',
      component: EditPhoto,
      props: true
    }
  ]
})

export default router
