<template>
  <div class="galleries-container">
    <div class="galleries-content">
      <div class="galleries-header">
        <h1>My Galleries</h1>
        <button @click="showCreateGalleryModal" class="create-gallery-btn">
          <font-awesome-icon icon="fa-solid fa-plus" /> Create Gallery
        </button>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-message">
        <p>{{ error }}</p>
        <button @click="loadGalleries" class="retry-button">Retry</button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Loading galleries...</p>
      </div>

      <!-- Galleries Grid -->
      <div v-else-if="galleries.length > 0" class="galleries-grid">
        <div 
          v-for="gallery in galleries" 
          :key="gallery.id" 
          class="gallery-card"
        >
          <div class="gallery-content" @click="navigateToGallery(gallery.id)">
            <div class="gallery-preview">
              <div v-if="gallery.previewPhotos && gallery.previewPhotos.length > 0" class="preview-grid">
                <div 
                  v-for="(photo, index) in gallery.previewPhotos.slice(0, 4)" 
                  :key="photo.id"
                  class="preview-photo"
                  :style="{ backgroundImage: `url(${getPhotoUrl(photo.id)})` }"
                ></div>
                <div 
                  v-for="i in Math.max(0, 4 - gallery.previewPhotos.length)" 
                  :key="`empty-${i}`"
                  class="preview-photo empty"
                ></div>
              </div>
              <div v-else class="no-photos">
                <font-awesome-icon icon="fa-solid fa-images" />
                <span>No photos yet</span>
              </div>
            </div>
            
            <div class="gallery-info">
              <h3 class="gallery-name">{{ gallery.name }}</h3>
              <p v-if="gallery.description" class="gallery-description">{{ gallery.description }}</p>
              <div class="gallery-meta">
                <span class="photo-count">{{ gallery.photoCount }} photo{{ gallery.photoCount === 1 ? '' : 's' }}</span>
                <span class="created-date">{{ formatDate(gallery.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <div class="gallery-actions" @click.stop>
            <button @click="showEditGalleryModal(gallery)" class="action-btn edit-btn" title="Edit Gallery">
              <font-awesome-icon icon="fa-solid fa-edit" />
            </button>
            <button @click="showDeleteGalleryModal(gallery)" class="action-btn delete-btn" title="Delete Gallery">
              <font-awesome-icon icon="fa-solid fa-trash" />
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <font-awesome-icon icon="fa-solid fa-folder-open" />
        </div>
        <h2>No Galleries Yet</h2>
        <p>Create your first gallery to start organizing your photos!</p>
        <button @click="showCreateGalleryModal" class="create-first-gallery-btn">
          <font-awesome-icon icon="fa-solid fa-plus" /> Create Your First Gallery
        </button>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiService } from '@/services/api'
import { useModalStore } from '@/stores/modalStore'

interface Gallery {
  id: number
  name: string
  description: string
  userId: number
  photoCount: number
  createdAt: string
  updatedAt: string
  previewPhotos: Photo[]
}

interface Photo {
  id: number
  title: string
  description: string
  originalFilename: string
  contentType: string
  size: number
  userId: number
  galleryId?: number
}

const router = useRouter()
const modalStore = useModalStore()

// State
const galleries = ref<Gallery[]>([])
const loading = ref(false)
const error = ref('')
const photoUrls = ref<Map<number, string>>(new Map())

// Methods
const loadGalleries = async () => {
  try {
    loading.value = true
    error.value = ''
    const response = await apiService.getGalleries()
    galleries.value = response
    
    // Load preview images for all galleries
    for (const gallery of response) {
      if (gallery.previewPhotos && gallery.previewPhotos.length > 0) {
        for (const photo of gallery.previewPhotos.slice(0, 4)) {
          await loadPhotoImage(photo.id)
        }
      }
    }
  } catch (err: any) {
    console.error('Error loading galleries:', err)
    error.value = 'Failed to load galleries. Please try again.'
  } finally {
    loading.value = false
  }
}

const showCreateGalleryModal = () => {
  modalStore.showCreateGalleryModal(
    async (name: string, description: string) => {
      await apiService.createGallery({
        name: name.trim(),
        description: description.trim()
      })
      await loadGalleries()
    },
    () => {
      // Optional cancel callback
    }
  )
}

const navigateToGallery = (galleryId: number) => {
  router.push(`/gallery/${galleryId}`)
}

const getPhotoUrl = (photoId: number): string => {
  return photoUrls.value.get(photoId) || ''
}

const loadPhotoImage = async (photoId: number) => {
  if (photoUrls.value.has(photoId)) {
    return // Already loaded
  }
  
  try {
    const blob = await apiService.getPhotoImage(photoId)
    const url = URL.createObjectURL(blob)
    photoUrls.value.set(photoId, url)
  } catch (error) {
    console.error('Error loading photo image:', error)
  }
}

const showEditGalleryModal = (gallery: Gallery) => {
  modalStore.showEditGalleryModal(
    gallery.id,
    gallery.name,
    gallery.description || '',
    async (galleryId: number, name: string, description: string) => {
      await apiService.updateGallery(galleryId, { name, description })
      await loadGalleries()
    },
    () => {
      // Optional cancel callback
    }
  )
}

const showDeleteGalleryModal = (gallery: Gallery) => {
  modalStore.showDeleteGalleryModal(
    gallery.id,
    gallery.name,
    async (galleryId: number, deleteOption: string) => {
      await apiService.deleteGallery(galleryId)
      await loadGalleries()
    },
    () => {
      // Optional cancel callback
    }
  )
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  loadGalleries()
})

onUnmounted(() => {
  // Clean up object URLs to prevent memory leaks
  photoUrls.value.forEach(url => {
    URL.revokeObjectURL(url)
  })
  photoUrls.value.clear()
})
</script>

<style scoped>
.galleries-container {
  width: 100%;
  max-width: 100%;
  margin: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  overflow: visible;
}

.galleries-content {
  width: 100%;
  max-width: 100%;
  margin: 0;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  flex: 1;
  position: static;
  padding: 0 2rem;
  padding-bottom: 2rem;
}

.galleries-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 2rem 0 1rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.galleries-header h1 {
  font-size: 2.5rem;
  color: #1a1a1a;
  margin: 0;
  font-weight: 700;
}

.create-gallery-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.create-gallery-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.galleries-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin: 2rem 0;
}

.gallery-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.gallery-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.gallery-preview {
  height: 200px;
  background: #f8fafc;
  position: relative;
}

.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  height: 100%;
  gap: 2px;
}

.preview-photo {
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.preview-photo.empty {
  background: #e5e7eb;
}

.no-photos {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  font-size: 1.5rem;
  gap: 0.5rem;
}

.no-photos span {
  font-size: 0.875rem;
}

.gallery-info {
  padding: 1.5rem;
}

.gallery-name {
  font-size: 1.25rem;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.gallery-description {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0 0 1rem 0;
  line-height: 1.4;
}

.gallery-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #9ca3af;
}

.photo-count {
  font-weight: 500;
}

.gallery-content {
  cursor: pointer;
}

.gallery-actions {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e5e7eb;
  justify-content: flex-end;
}

.action-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
  font-size: 0.875rem;
}

.action-btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.edit-btn:hover {
  color: #2563eb;
}

.delete-btn:hover {
  color: #dc2626;
}

.created-date {
  font-style: italic;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  color: #d1d5db;
  margin-bottom: 1rem;
}

.empty-state h2 {
  font-size: 1.5rem;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.empty-state p {
  color: #6b7280;
  margin: 0 0 2rem 0;
}

.create-first-gallery-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.create-first-gallery-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 1rem;
  border-radius: 8px;
  margin: 2rem 0;
  text-align: center;
}

.retry-button {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  margin-top: 0.5rem;
  cursor: pointer;
}

.loading-container {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}



.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .galleries-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .galleries-header h1 {
    font-size: 2rem;
  }
  
  .galleries-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  

}
</style>
