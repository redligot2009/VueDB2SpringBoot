<template>
  <div class="photo-card" :class="{ 'selected': isSelected }">
    <!-- Selection checkbox -->
    <div class="selection-overlay" v-if="showSelection">
      <input 
        type="checkbox" 
        :checked="isSelected"
        @change="toggleSelection"
        class="selection-checkbox"
        :aria-label="`Select ${photo.title}`"
      />
    </div>
    
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
             <div class="photo-actions">
         <RouterLink :to="`/photo/${photo.id}`" class="view-btn">
           <font-awesome-icon icon="fa-solid fa-eye" /> View
         </RouterLink>
         <RouterLink :to="`/edit/${photo.id}`" class="edit-btn">
           <font-awesome-icon icon="fa-solid fa-edit" /> Edit
         </RouterLink>
         <button @click="showDeleteModal" class="delete-btn">
           <font-awesome-icon icon="fa-solid fa-trash" /> Delete
         </button>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { apiService, type Photo } from '@/services/api'
import { usePhotoStore } from '@/stores/photoStore'
import { useModalStore } from '@/stores/modalStore'

interface Props {
  photo: Photo
  showSelection?: boolean
  isSelected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSelection: false,
  isSelected: false
})

const emit = defineEmits<{
  toggleSelection: [photoId: number]
}>()
const photoStore = usePhotoStore()
const modalStore = useModalStore()

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

const handleDelete = async () => {
  try {
    await photoStore.deletePhoto(props.photo.id)
  } catch (error) {
    console.error('Error deleting photo:', error)
    // The error will be handled by the store and displayed in the gallery
  }
}

const showDeleteModal = () => {
  modalStore.showDeleteModal(
    `Are you sure you want to delete '${props.photo.title}'?`,
    handleDelete
  )
}

const toggleSelection = () => {
  emit('toggleSelection', props.photo.id)
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
  height: fit-content;
  display: flex;
  flex-direction: column;
  position: relative;
}

.photo-card.selected {
  border: 2px solid #2563eb;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.3);
}

.selection-overlay {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px;
}

.selection-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #2563eb;
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
  flex-shrink: 0;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
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
  flex: 1;
  display: flex;
  flex-direction: column;
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
  flex: 1;
}

.photo-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.photo-size,
.photo-type {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.photo-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.view-btn {
  background: #059669;
  color: white;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.view-btn:hover {
  background: #047857;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(5, 150, 105, 0.2);
}

.edit-btn {
  background: #2563eb;
  color: white;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.edit-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.delete-btn {
  background: #dc2626;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  flex: 1;
  justify-content: center;
}

.delete-btn:hover:not(:disabled) {
  background: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.2);
}

.delete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .photo-image-container {
    height: 180px;
  }
  
  .photo-info {
    padding: 12px;
  }
  
  .photo-title {
    font-size: 16px;
  }
  
  .photo-description {
    font-size: 13px;
  }
  
  .photo-actions {
    gap: 6px;
  }
  
  .edit-btn,
  .delete-btn {
    padding: 5px 10px;
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .photo-image-container {
    height: 160px;
  }
  
  .photo-info {
    padding: 10px;
  }
  
  .photo-title {
    font-size: 15px;
  }
  
  .photo-description {
    font-size: 12px;
    -webkit-line-clamp: 3;
  }
  
  .photo-meta {
    font-size: 11px;
    gap: 8px;
  }
  
  .photo-actions {
    flex-direction: column;
    gap: 4px;
  }
  
  .edit-btn,
  .delete-btn {
    padding: 6px 8px;
    font-size: 10px;
    justify-content: center;
  }
}
</style>
