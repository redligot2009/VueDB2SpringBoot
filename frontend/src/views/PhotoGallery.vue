<template>
  <div class="photo-gallery">
    <div class="gallery-header">
      <h1>Photo Gallery</h1>
      <div class="gallery-stats">
        <span v-if="photoStore.loading" class="loading-text">Loading...</span>
        <span v-else-if="photoStore.hasPhotos" class="photo-count">
          {{ photoStore.photoCount }} photo{{ photoStore.photoCount !== 1 ? 's' : '' }}
        </span>
        <span v-else class="no-photos">No photos found</span>
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
      <div class="empty-icon">📷</div>
      <h2>No Photos Yet</h2>
      <p>Upload some photos to get started!</p>
    </div>

    <!-- Refresh Button -->
    <div class="refresh-section">
      <button @click="refreshPhotos" class="refresh-button" :disabled="photoStore.loading">
        <span v-if="photoStore.loading">Refreshing...</span>
        <span v-else>Refresh Gallery</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePhotoStore } from '@/stores/photoStore'
import PhotoCard from '@/components/PhotoCard.vue'

const photoStore = usePhotoStore()

const loadPhotos = async () => {
  await photoStore.fetchPhotos()
}

const refreshPhotos = async () => {
  await loadPhotos()
}

const retryLoad = async () => {
  photoStore.clearError()
  await loadPhotos()
}

onMounted(() => {
  loadPhotos()
})
</script>

<style scoped>
.photo-gallery {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.gallery-header {
  text-align: center;
  margin-bottom: 40px;
}

.gallery-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px 0;
}

.gallery-stats {
  font-size: 1.1rem;
  color: #666;
}

.loading-text {
  color: #007bff;
}

.photo-count {
  color: #28a745;
  font-weight: 500;
}

.no-photos {
  color: #dc3545;
}

.error-message {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  text-align: center;
  color: #721c24;
}

.error-message p {
  margin: 0 0 12px 0;
}

.retry-button {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background: #c82333;
}

.loading-container {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 1.5rem;
  margin: 0 0 8px 0;
  color: #333;
}

.empty-state p {
  font-size: 1.1rem;
  margin: 0;
}

.refresh-section {
  text-align: center;
  margin-top: 40px;
}

.refresh-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.refresh-button:hover:not(:disabled) {
  background: #0056b3;
}

.refresh-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* Responsive Design */
@media (max-width: 768px) {
  .photo-gallery {
    padding: 16px;
  }
  
  .gallery-header h1 {
    font-size: 2rem;
  }
  
  .photo-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .photo-grid {
    grid-template-columns: 1fr;
  }
}
</style>
