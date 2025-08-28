<template>
  <div class="photo-card">
    <div class="photo-image-container">
      <img 
        v-if="imageUrl" 
        :src="imageUrl" 
        :alt="photo.title"
        class="photo-image"
        @error="handleImageError"
      />
      <div v-else class="photo-placeholder">
        <span>No Image</span>
      </div>
    </div>
    <div class="photo-info">
      <h3 class="photo-title">{{ photo.title }}</h3>
      <p v-if="photo.description" class="photo-description">
        {{ photo.description }}
      </p>
      <div class="photo-meta">
        <span v-if="photo.size" class="photo-size">
          {{ formatFileSize(photo.size) }}
        </span>
        <span v-if="photo.contentType" class="photo-type">
          {{ photo.contentType }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiService, type Photo } from '@/services/api'

interface Props {
  photo: Photo
}

const props = defineProps<Props>()

const imageUrl = ref<string | null>(null)
const imageError = ref(false)

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const loadImage = async () => {
  try {
    const blob = await apiService.getPhotoImage(props.photo.id)
    imageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('Error loading image:', error)
    imageError.value = true
  }
}

const handleImageError = () => {
  imageError.value = true
  imageUrl.value = null
}

onMounted(() => {
  loadImage()
})
</script>

<style scoped>
.photo-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.photo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.photo-image-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.photo-card:hover .photo-image {
  transform: scale(1.05);
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #666;
  font-size: 14px;
}

.photo-info {
  padding: 16px;
}

.photo-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.photo-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.photo-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.photo-size,
.photo-type {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
