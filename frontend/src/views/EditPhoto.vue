<template>
  <div class="edit-photo">
    <div class="edit-header">
      <h1>Edit Photo</h1>
      <p>Update your photo details and image</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading photo...</p>
    </div>

    <!-- Error Message -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="retryLoad" class="retry-button">Retry</button>
    </div>

    <!-- Edit Form -->
    <form v-else-if="photo" @submit.prevent="handleSubmit" class="edit-form">
      <div class="current-image-section">
        <h3>Current Image</h3>
        <div class="current-image-container">
          <img 
            v-if="currentImageUrl" 
            :src="currentImageUrl" 
            :alt="photo.title"
            class="current-image"
            @error="handleImageError"
          />
          <div v-else class="image-placeholder">
            <span>No Image</span>
          </div>
        </div>
        <div class="current-image-info">
          <p><strong>Title:</strong> {{ photo.title }}</p>
          <p v-if="photo.description"><strong>Description:</strong> {{ photo.description }}</p>
          <p v-if="photo.size"><strong>Size:</strong> {{ formatFileSize(photo.size) }}</p>
          <p v-if="photo.contentType"><strong>Type:</strong> {{ photo.contentType }}</p>
        </div>
      </div>

      <div class="form-section">
        <h3>Update Details</h3>
        
        <div class="form-group">
          <label for="title">Title *</label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            required
            placeholder="Enter photo title"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea
            id="description"
            v-model="formData.description"
            placeholder="Enter photo description (optional)"
            rows="3"
            class="form-textarea"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="file">New Image (Optional)</label>
          <div class="file-upload-area" :class="{ 'drag-over': isDragOver, 'has-file': selectedFile, 'file-too-large': isFileTooLarge }">
            <input 
              ref="fileInput" 
              type="file" 
              accept="image/*" 
              @change="handleFileSelect" 
              class="file-input"
            />
                         <div v-if="!selectedFile" class="upload-placeholder">
                               <span>📁 Click to browse or drag and drop</span>
               <small>Maximum file size: 8MB • Supported formats: JPG, PNG, GIF, WebP</small>
             </div>
             <div v-else-if="isFileTooLarge" class="file-error">
                               <span>⚠️ File too large</span>
               <small>{{ getFileStatusMessage() }}</small>
             </div>
             <div v-else class="file-preview">
                               <span>✅ {{ selectedFile.name }}</span>
               <small>{{ formatFileSize(selectedFile.size) }}</small>
             </div>
            <button type="button" @click="triggerFileInput" class="browse-btn">
              Browse Files
            </button>
          </div>
          <div v-if="selectedFile && !isFileTooLarge" class="file-actions">
            <button type="button" @click="removeFile" class="remove-btn">
              Remove File
            </button>
          </div>
        </div>
      </div>

             <div class="form-actions">
         <button type="button" @click="showDeleteModal" class="delete-btn">
                       🗑️ Delete Photo
         </button>
        <button type="button" @click="handleCancel" class="cancel-btn">
          Cancel
        </button>
        <button type="submit" :disabled="!canSubmit || submitting" class="save-btn">
          <span v-if="submitting">Saving...</span>
          <span v-else>Save Changes</span>
        </button>
      </div>
    </form>

         <!-- Not Found -->
     <div v-else class="not-found">
       <h2>Photo Not Found</h2>
       <p>The photo you're looking for doesn't exist.</p>
       <RouterLink to="/" class="back-btn">Back to Gallery</RouterLink>
     </div>
   </div>
 </template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { usePhotoStore } from '@/stores/photoStore'
import { useModalStore } from '@/stores/modalStore'
import { apiService, type Photo } from '@/services/api'

const route = useRoute()
const router = useRouter()
const photoStore = usePhotoStore()
const modalStore = useModalStore()

// State
const photo = ref<Photo | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const submitting = ref(false)
const currentImageUrl = ref<string | null>(null)
const imageError = ref(false)

// Form data
const formData = ref({
  title: '',
  description: ''
})

// File upload state
const fileInput = ref<HTMLInputElement>()
const selectedFile = ref<File | null>(null)
const isDragOver = ref(false)

// Computed
const isFileTooLarge = computed(() => {
  if (!selectedFile.value) return false
  return selectedFile.value.size > 8 * 1024 * 1024 // 8MB
})

const canSubmit = computed(() => {
  return formData.value.title.trim() && !submitting.value
})

// Methods
const loadPhoto = async () => {
  const photoId = Number(route.params.id)
  if (isNaN(photoId)) {
    error.value = 'Invalid photo ID'
    loading.value = false
    return
  }

  try {
    photo.value = await apiService.getPhotoById(photoId)
    formData.value.title = photo.value.title
    formData.value.description = photo.value.description || ''
    await loadCurrentImage(photoId)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to load photo'
    console.error('Error loading photo:', err)
  } finally {
    loading.value = false
  }
}

const loadCurrentImage = async (photoId: number) => {
  try {
    const blob = await apiService.getPhotoImage(photoId)
    currentImageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('Error loading current image:', error)
    imageError.value = true
  }
}

const retryLoad = async () => {
  error.value = null
  loading.value = true
  await loadPhoto()
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    setSelectedFile(target.files[0])
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    setSelectedFile(event.dataTransfer.files[0])
  }
}

const setSelectedFile = (file: File) => {
  selectedFile.value = file
}

const removeFile = () => {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileStatusMessage = (): string => {
  if (!selectedFile.value) return ''
  return `File size (${formatFileSize(selectedFile.value.size)}) exceeds the maximum limit of 8MB`
}

const handleImageError = () => {
  imageError.value = true
  currentImageUrl.value = null
}

const handleSubmit = async () => {
  if (!photo.value || !canSubmit.value) return

  submitting.value = true
  error.value = null

  try {
    await photoStore.updatePhoto(
      photo.value.id,
      formData.value.title,
      formData.value.description,
      selectedFile.value || undefined
    )
    
    // Navigate back to gallery
    router.push('/')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to update photo'
    console.error('Error updating photo:', err)
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/')
}

const handleDelete = async () => {
  if (!photo.value) return

  error.value = null

  try {
    await photoStore.deletePhoto(photo.value.id)
    router.push('/')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to delete photo'
    console.error('Error deleting photo:', err)
  }
}

const showDeleteModal = () => {
  if (!photo.value) return
  
  modalStore.showDeleteModal(
    `Are you sure you want to delete '${photo.value.title}'? This action cannot be undone.`,
    handleDelete
  )
}

onMounted(() => {
  loadPhoto()
})
</script>

<style scoped>
.edit-photo {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.edit-header {
  text-align: center;
  padding: 2rem 0 1rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.edit-header h1 {
  font-size: 2.5rem;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.edit-header p {
  color: #4a5568;
  font-size: 1.1rem;
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

.edit-form {
  padding: 0 0 2rem 0;
}

.current-image-section,
.form-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.current-image-section h3,
.form-section h3 {
  color: #1a1a1a;
  margin-bottom: 1rem;
  font-weight: 600;
}

.current-image-container {
  width: 100%;
  height: auto;
  max-height: 60vh;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.current-image {
  width: 100%;
  height: auto;
  max-height: 60vh;
  object-fit: contain;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #666;
  font-size: 14px;
}

.current-image-info p {
  margin: 0.5rem 0;
  color: #4a5568;
  font-size: 0.9rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #1a1a1a;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.file-upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  position: relative;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.file-upload-area.drag-over {
  border-color: #2563eb;
  background: rgba(37, 99, 235, 0.05);
}

.file-upload-area.has-file {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.file-upload-area.file-too-large {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

.file-input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #6b7280;
}

.upload-placeholder span {
  font-size: 1rem;
  font-weight: 500;
}

.upload-placeholder small {
  font-size: 0.875rem;
}

.file-error {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #dc2626;
}

.file-error span {
  font-size: 1rem;
  font-weight: 500;
}

.file-error small {
  font-size: 0.875rem;
}

.file-preview {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #059669;
}

.file-preview span {
  font-size: 1rem;
  font-weight: 500;
}

.file-preview small {
  font-size: 0.875rem;
}

.browse-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  margin-top: 1rem;
}

.browse-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.file-actions {
  margin-top: 1rem;
  text-align: center;
}

.remove-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.delete-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.delete-btn:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.delete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.cancel-btn {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

.save-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.save-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.not-found {
  text-align: center;
  padding: 4rem 2rem;
  color: #4a5568;
}

.not-found h2 {
  font-size: 2rem;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.not-found p {
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.back-btn {
  background: #2563eb;
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-block;
}

.back-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

@media (max-width: 768px) {
  .edit-photo {
    padding: 0 1rem;
    margin: 0 0.5rem;
  }
  
  .edit-header h1 {
    font-size: 2rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .current-image-container {
    max-width: 100%;
    max-height: 50vh;
  }
  
  .current-image {
    max-height: 50vh;
  }
}

@media (max-width: 480px) {
  .edit-photo {
    margin: 0 0.25rem;
    padding: 0 0.75rem;
  }
  
  .edit-header h1 {
    font-size: 1.75rem;
  }
  
  .current-image-section,
  .form-section {
    padding: 1rem;
  }
}
</style>
