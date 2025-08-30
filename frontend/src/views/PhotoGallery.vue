<template>
  <div class="photo-gallery">
    <div class="gallery-content">
      <div class="gallery-header">
        <div class="gallery-title-section">
          <h1>Photo Gallery</h1>
          <div class="gallery-stats">
            <span v-if="photoStore.loading" class="loading-text">Loading...</span>
            <span v-else-if="photoStore.hasPhotos" class="photo-count">
              Showing {{ photoStore.photoCount }} of {{ photoStore.totalPhotoCount }} photos
              <span v-if="photoStore.totalPages > 1" class="page-info">
                (Page {{ photoStore.currentPage + 1 }} of {{ photoStore.totalPages }})
              </span>
            </span>
            <span v-else class="no-photos">No photos found</span>
          </div>
        </div>
                 <div class="gallery-actions">
                       <button @click="showBulkUpload" class="bulk-upload-btn">
              <font-awesome-icon icon="fa-solid fa-upload" /> Bulk Upload
            </button>
                       <RouterLink to="/upload" class="upload-btn">
              <font-awesome-icon icon="fa-solid fa-camera" /> Upload Photos
            </RouterLink>
         </div>
      </div>

      <!-- Error Message -->
      <div v-if="photoStore.error" class="error-message">
        <p>{{ photoStore.error }}</p>
        <button @click="retryLoad" class="retry-button">Retry</button>
      </div>

      <!-- Loading State -->
      <div v-if="photoStore.loading && !photoStore.hasPhotos" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Loading photos...</p>
      </div>

      <!-- Photo Grid -->
      <div v-else-if="photoStore.hasPhotos" class="photo-grid">
        <PhotoCard 
          v-for="photo in photoStore.photos" 
          :key="photo.id" 
          :photo="photo" 
        />
      </div>

             <!-- Empty State -->
       <div v-else class="empty-state">
         <div class="empty-icon">
           <font-awesome-icon icon="fa-solid fa-camera" />
         </div>
         <h2>No Photos Yet</h2>
         <p>Upload some photos to get started!</p>
                   <RouterLink to="/upload" class="upload-btn empty-upload-btn">
            <font-awesome-icon icon="fa-solid fa-camera" /> Upload Your First Photo
          </RouterLink>
       </div>

      <!-- Refresh Button -->
      <div class="refresh-section">
        <button @click="refreshPhotos" class="refresh-button" :disabled="photoStore.loading">
          <span v-if="photoStore.loading">Refreshing...</span>
          <span v-else>Refresh Gallery</span>
        </button>
      </div>
    </div>

    <!-- Sticky Pagination Controls -->
    <div v-if="photoStore.totalPages > 1" class="sticky-pagination">
      <div class="pagination-section">
        <div class="pagination-info">
          <span>Page {{ photoStore.currentPage + 1 }} of {{ photoStore.totalPages }}</span>
          <span>{{ photoStore.totalPhotoCount }} total photos</span>
        </div>
        
                 <div class="pagination-controls">
           <button 
             @click="goToFirstPage" 
             :disabled="photoStore.isFirstPage || photoStore.loading"
             class="pagination-btn first-btn"
             title="First Page"
           >
                           <font-awesome-icon icon="fa-solid fa-angle-double-left" />
           </button>
           
           <button 
             @click="goToPreviousPage" 
             :disabled="photoStore.isFirstPage || photoStore.loading"
             class="pagination-btn prev-btn"
             title="Previous Page"
           >
                           <font-awesome-icon icon="fa-solid fa-angle-left" />
           </button>
          
          <div class="page-numbers">
            <button 
              v-for="pageNum in visiblePageNumbers" 
              :key="pageNum"
              @click="goToPage(pageNum - 1)"
              :class="['page-btn', { active: pageNum - 1 === photoStore.currentPage }]"
              :disabled="photoStore.loading"
            >
              {{ pageNum }}
            </button>
          </div>
          
                     <button 
             @click="goToNextPage" 
             :disabled="photoStore.isLastPage || photoStore.loading"
             class="pagination-btn next-btn"
             title="Next Page"
           >
                           <font-awesome-icon icon="fa-solid fa-angle-right" />
           </button>
           
           <button 
             @click="goToLastPage" 
             :disabled="photoStore.isLastPage || photoStore.loading"
             class="pagination-btn last-btn"
             title="Last Page"
           >
                           <font-awesome-icon icon="fa-solid fa-angle-double-right" />
           </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { usePhotoStore } from '@/stores/photoStore'
import { useModalStore } from '@/stores/modalStore'
import PhotoCard from '@/components/PhotoCard.vue'

const photoStore = usePhotoStore()
const modalStore = useModalStore()

// Computed properties for pagination
const visiblePageNumbers = computed(() => {
  const current = photoStore.currentPage + 1
  const total = photoStore.totalPages
  const maxVisible = 5
  
  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  
  let start = Math.max(1, current - Math.floor(maxVisible / 2))
  let end = Math.min(total, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const loadPhotos = async () => {
  try {
    await photoStore.fetchPhotos()
  } catch (error) {
    console.error('Failed to load photos:', error)
  }
}

const refreshPhotos = async () => {
  await loadPhotos()
}

const retryLoad = async () => {
  photoStore.clearError()
  await loadPhotos()
}

// Pagination methods
const goToPage = async (page: number) => {
  try {
    await photoStore.goToPage(page)
  } catch (error) {
    console.error('Failed to navigate to page:', error)
  }
}

const goToNextPage = async () => {
  try {
    await photoStore.fetchNextPage()
  } catch (error) {
    console.error('Failed to go to next page:', error)
  }
}

const goToPreviousPage = async () => {
  try {
    await photoStore.fetchPreviousPage()
  } catch (error) {
    console.error('Failed to go to previous page:', error)
  }
}

const goToFirstPage = async () => {
  await goToPage(0)
}

const goToLastPage = async () => {
  await goToPage(photoStore.totalPages - 1)
}

// Bulk upload method
const showBulkUpload = () => {
  modalStore.showBulkUploadModal(
    async (files: File[], titles?: string[], descriptions?: string[]) => {
      await photoStore.bulkAddPhotos(files, titles, descriptions)
      // Refresh the gallery after bulk upload
      await loadPhotos()
    },
    () => {
      // Optional cancel callback
    }
  )
}

onMounted(() => {
  loadPhotos()
})
</script>

<style scoped>
.photo-gallery {
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.gallery-content {
  flex: 1;
  padding: 0 2rem;
  padding-bottom: 0; /* Remove bottom padding to prevent overlap with sticky pagination */
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 2rem;
  padding: 2rem 0 1rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.gallery-title-section h1 {
  font-size: 2.5rem;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.gallery-stats {
  color: #4a5568;
  font-size: 1.1rem;
  font-weight: 500;
}

.page-info {
  color: #6b7280;
  font-size: 0.9rem;
  margin-left: 0.5rem;
}

.gallery-actions {
  display: flex;
  gap: 1rem;
}

.bulk-upload-btn {
  background: #9b59b6;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(155, 89, 182, 0.2);
  cursor: pointer;
}

.bulk-upload-btn:hover {
  background: #8e44ad;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(155, 89, 182, 0.3);
}

.upload-btn {
  background: #2563eb;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

.upload-btn:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 2rem;
  color: #dc2626;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
}

.retry-button {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.retry-button:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

.loading-container {
  text-align: center;
  padding: 4rem 2rem;
  color: #4a5568;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 0 0 2rem 0;
  width: 100%;
  max-width: 80vw;
  margin-left: auto;
  margin-right: auto;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #4a5568;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  margin: 2rem 0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: #6b7280;
}

.empty-icon i {
  font-size: 4rem;
}

.empty-state h2 {
  font-size: 2rem;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.empty-state p {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  color: #4a5568;
}

.empty-upload-btn {
  font-size: 1.1rem;
  padding: 1rem 2rem;
}

/* Sticky Pagination Styles */
.sticky-pagination {
  position: sticky;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 100;
  margin-top: auto;
}

.pagination-section {
  padding: 1.5rem 2rem;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 0 0 12px 12px;
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  color: #4a5568;
  font-size: 0.9rem;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.pagination-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
  transform: translateY(-1px);
}

.page-btn.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.page-btn.active:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.refresh-section {
  text-align: center;
  margin-top: 2rem;
  padding: 1rem 0;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.refresh-button {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.refresh-button:hover:not(:disabled) {
  background: #4b5563;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .photo-gallery {
    margin: 0;
    border-radius: 0;
    max-width: 100%;
    width: 100%;
  }
  
  .gallery-content {
    padding: 0 1rem;
  }
  
  .gallery-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
    padding: 1.5rem 0 1rem 0;
  }
  
  .gallery-title-section h1 {
    font-size: 2rem;
  }
  
  .photo-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
    padding: 0 0 1.5rem 0;
    max-width: 100%;
    margin-left: 0;
    margin-right: 0;
  }
  
  .upload-btn {
    justify-content: center;
  }
  
  .empty-state {
    margin: 1.5rem 0;
    padding: 3rem 1rem;
  }
  
  .empty-state h2 {
    font-size: 1.75rem;
  }
  
  .pagination-section {
    padding: 1rem;
  }
  
  .pagination-controls {
    gap: 0.25rem;
  }
  
  .pagination-btn,
  .page-btn {
    padding: 0.4rem 0.6rem;
    min-width: 36px;
    font-size: 0.9rem;
  }
  
  .pagination-info {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .photo-gallery {
    margin: 0;
    border-radius: 0;
    max-width: 100%;
    width: 100%;
  }
  
  .gallery-content {
    padding: 0 0.75rem;
  }
  
  .gallery-header {
    padding: 1rem 0 0.75rem 0;
  }
  
  .gallery-title-section h1 {
    font-size: 1.75rem;
  }
  
  .empty-state {
    padding: 2rem 0.75rem;
  }
  
  .empty-state h2 {
    font-size: 1.5rem;
  }
  
  .photo-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
    max-width: 100%;
    margin-left: 0;
    margin-right: 0;
  }
  
  .pagination-section {
    padding: 0.75rem;
  }
  
  .page-numbers {
    gap: 0.125rem;
  }
  
  .pagination-btn,
  .page-btn {
    padding: 0.35rem 0.5rem;
    min-width: 32px;
    font-size: 0.8rem;
  }
}
</style>
