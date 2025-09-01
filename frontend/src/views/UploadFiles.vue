<template>
  <div class="upload-files">
    <div class="upload-header">
      <h1>Upload Files</h1>
      <p>Drag and drop your photos here or click to browse</p>
      <p class="upload-limits">Maximum file size: 8MB • Bulk upload: up to 20 files, 100MB total • Supported formats: JPG, PNG, GIF, WebP</p>
      <div class="upload-options">
        <button 
          type="button" 
          @click="showBulkUpload" 
          class="btn btn-secondary bulk-upload-btn"
        >
                     <font-awesome-icon icon="fa-solid fa-upload" /> Bulk Upload Multiple Photos
        </button>
      </div>
    </div>

    <form @submit.prevent="handleUpload" class="upload-form">
      <!-- File Upload Area -->
      <div 
        class="file-upload-area"
        :class="{ 'drag-over': isDragOver, 'has-file': selectedFile, 'file-too-large': isFileTooLarge }"
        @drop="handleDrop"
        @dragover.prevent="isDragOver = true"
        @dragleave.prevent="isDragOver = false"
        @click="triggerFileInput"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          @change="handleFileSelect"
          class="file-input"
        />
        
        <div v-if="!selectedFile" class="upload-placeholder">
                     <div class="upload-icon">
             <font-awesome-icon icon="fa-solid fa-folder-open" />
           </div>
          <p>Click to select or drag and drop an image here</p>
          <p class="upload-hint">Supports: JPG, PNG, GIF, WebP</p>
        </div>
        
        <div v-else-if="isFileTooLarge" class="file-error">
                     <div class="error-icon">
             <font-awesome-icon icon="fa-solid fa-exclamation-triangle" />
           </div>
          <div class="error-info">
            <p class="error-title">File Too Large</p>
            <p class="error-message">{{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})</p>
            <p class="error-detail">Maximum allowed size is 8MB. Please choose a smaller file.</p>
          </div>
          <button type="button" @click="removeFile" class="remove-file-btn">
            <font-awesome-icon icon="fa-solid fa-times" />
          </button>
        </div>
        
        <div v-else class="file-preview">
          <img :src="filePreview" :alt="selectedFile.name" class="preview-image" />
          <div class="file-info">
            <p class="file-name">{{ selectedFile.name }}</p>
            <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
            <p class="file-status" :class="{ 'size-warning': isFileSizeWarning }">
              {{ getFileStatusMessage() }}
            </p>
          </div>
          <button type="button" @click="removeFile" class="remove-file-btn">
            <font-awesome-icon icon="fa-solid fa-times" />
          </button>
        </div>
      </div>

      <!-- Form Fields -->
      <div class="form-fields">
        <div class="form-group">
          <label for="title" class="form-label">Title *</label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            required
            placeholder="Enter photo title"
            class="form-input"
            :disabled="isUploading"
          />
        </div>

        <div class="form-group">
          <label for="description" class="form-label">Description</label>
          <textarea
            id="description"
            v-model="formData.description"
            placeholder="Enter photo description (optional)"
            class="form-textarea"
            rows="3"
            :disabled="isUploading"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="gallery" class="form-label">Upload to Gallery</label>
          <select
            id="gallery"
            v-model="formData.galleryId"
            class="form-select"
            :disabled="isUploading || loadingGalleries"
          >
            <option :value="null">Unorganized Photos</option>
            <option 
              v-for="gallery in galleries" 
              :key="gallery.id" 
              :value="gallery.id"
            >
              {{ gallery.name }} ({{ gallery.photoCount }} photos)
            </option>
          </select>
          <div v-if="loadingGalleries" class="loading-text">Loading galleries...</div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="form-actions">
        <button
          type="button"
          @click="handleCancel"
          class="btn btn-secondary"
          :disabled="isUploading"
        >
          Cancel
        </button>
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="!canUpload || isUploading"
        >
          <span v-if="isUploading" class="loading-spinner"></span>
          {{ isUploading ? 'Uploading...' : 'Upload' }}
        </button>
      </div>
    </form>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="clearError" class="error-close"><font-awesome-icon icon="fa-solid fa-times" /></button>
    </div>

    <!-- Bulk Upload Modal is now handled globally in App.vue -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePhotoStore } from '@/stores/photoStore'
import { useModalStore } from '@/stores/modalStore'
import { apiService } from '@/services/api'

const router = useRouter()
const photoStore = usePhotoStore()
const modalStore = useModalStore()

// Constants
const MAX_FILE_SIZE = 8 * 1024 * 1024 // 8MB in bytes
const WARNING_FILE_SIZE = 5 * 1024 * 1024 // 5MB warning threshold

// Refs
const fileInput = ref<HTMLInputElement>()
const selectedFile = ref<File | null>(null)
const filePreview = ref<string>('')
const isDragOver = ref(false)
const isUploading = ref(false)
const error = ref<string | null>(null)

// Form data
const formData = ref({
  title: '',
  description: '',
  galleryId: null as number | null
})

// Gallery selection
const galleries = ref<any[]>([])
const loadingGalleries = ref(false)

// Computed
const isFileTooLarge = computed(() => {
  return selectedFile.value && selectedFile.value.size > MAX_FILE_SIZE
})

const isFileSizeWarning = computed(() => {
  return selectedFile.value && selectedFile.value.size > WARNING_FILE_SIZE && selectedFile.value.size <= MAX_FILE_SIZE
})

const canUpload = computed(() => {
  return selectedFile.value && 
         !isFileTooLarge.value && 
         formData.value.title.trim() && 
         !isUploading.value
})

// Methods
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    setSelectedFile(file)
  }
}

const handleDrop = (event: DragEvent) => {
  isDragOver.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.type.startsWith('image/')) {
      setSelectedFile(file)
    } else {
      error.value = 'Please select an image file (JPG, PNG, GIF, WebP)'
    }
  }
}

const setSelectedFile = (file: File) => {
  selectedFile.value = file
  
  // Create preview only if file is not too large
  if (file.size <= MAX_FILE_SIZE) {
    const reader = new FileReader()
    reader.onload = (e) => {
      filePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
  } else {
    filePreview.value = ''
  }
  
  // Auto-fill title if empty
  if (!formData.value.title) {
    const nameWithoutExt = file.name.replace(/\.[^/.]+$/, '')
    formData.value.title = nameWithoutExt
  }
  
  clearError()
}

const removeFile = () => {
  selectedFile.value = null
  filePreview.value = ''
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
  
  const size = selectedFile.value.size
  if (size > MAX_FILE_SIZE) {
    return 'File too large'
  } else if (size > WARNING_FILE_SIZE) {
    return 'Large file - upload may take longer'
  } else {
    return 'Ready to upload'
  }
}

const handleUpload = async () => {
  if (!selectedFile.value || !formData.value.title.trim()) {
    return
  }

  if (isFileTooLarge.value) {
    error.value = `File size (${formatFileSize(selectedFile.value.size)}) exceeds the maximum limit of 8MB`
    return
  }

  isUploading.value = true
  error.value = null

  try {
    await photoStore.addPhoto(
      formData.value.title.trim(),
      formData.value.description.trim(),
      selectedFile.value,
      formData.value.galleryId || undefined
    )
    
    // Reset form
    formData.value = { title: '', description: '', galleryId: null }
    removeFile()
    
    // Redirect to gallery
    router.push('/')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Upload failed'
  } finally {
    isUploading.value = false
  }
}

const handleCancel = () => {
  router.push('/')
}

const clearError = () => {
  error.value = null
}

const loadGalleries = async () => {
  try {
    loadingGalleries.value = true
    const response = await apiService.getGalleries()
    galleries.value = response
  } catch (err) {
    console.error('Error loading galleries:', err)
    error.value = 'Failed to load galleries'
  } finally {
    loadingGalleries.value = false
  }
}

// Bulk upload methods
const showBulkUpload = () => {
  modalStore.showBulkUploadModal(
    async (files: File[], titles?: string[], descriptions?: string[]) => {
      await photoStore.bulkAddPhotos(files, titles, descriptions)
      router.push('/')
    },
    () => {
      // Optional cancel callback
    }
  )
}

// Clear any existing errors when component mounts
onMounted(() => {
  clearError()
  loadGalleries()
})
</script>

<style scoped>
.upload-files {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.upload-header {
  text-align: center;
  margin-bottom: 2rem;
}

.upload-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.upload-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.upload-limits {
  font-size: 0.9rem !important;
  color: #95a5a6 !important;
  font-style: italic;
}

.upload-options {
  margin-top: 1rem;
}

.bulk-upload-btn {
  background: #9b59b6 !important;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.bulk-upload-btn:hover {
  background: #8e44ad !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(155, 89, 182, 0.3);
}

.upload-form {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.file-upload-area {
  border: 3px dashed #bdc3c7;
  border-radius: 8px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 2rem;
  position: relative;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-upload-area:hover {
  border-color: #3498db;
  background-color: #f8f9fa;
}

.file-upload-area.drag-over {
  border-color: #3498db;
  background-color: #ecf0f1;
  transform: scale(1.02);
}

.file-upload-area.has-file {
  border-style: solid;
  border-color: #27ae60;
  background-color: #f8fff9;
}

.file-upload-area.file-too-large {
  border-color: #e74c3c;
  background-color: #fdf2f2;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.upload-placeholder {
  color: #7f8c8d;
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #6b7280;
}

.upload-icon i {
  font-size: 3rem;
}

.upload-hint {
  font-size: 0.9rem;
  color: #95a5a6;
  margin-top: 0.5rem;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
}

.file-error {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
  color: #e74c3c;
}

.error-icon {
  font-size: 2rem;
  color: #dc2626;
}

.error-icon i {
  font-size: 2rem;
}

.error-info {
  flex: 1;
  text-align: left;
}

.error-title {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.error-message {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.error-detail {
  font-size: 0.9rem;
  color: #c0392b;
}

.preview-image {
  width: 80px;
  height: 80px;
  object-fit: contain;
  border-radius: 8px;
  border: 2px solid #ecf0f1;
}

.file-info {
  flex: 1;
  text-align: left;
}

.file-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
  word-break: break-word;
}

.file-size {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.file-status {
  font-size: 0.85rem;
  color: #27ae60;
  font-weight: 500;
}

.file-status.size-warning {
  color: #f39c12;
}

.remove-file-btn {
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s ease;
}

.remove-file-btn:hover {
  background: #c0392b;
}

.form-fields {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #3498db;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
  background-color: white;
  cursor: pointer;
}

.loading-text {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-top: 0.5rem;
  font-style: italic;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #7f8c8d;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  color: #c33;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.error-close {
  background: none;
  border: none;
  color: #c33;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-close:hover {
  background: #fcc;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .upload-files {
    padding: 1rem;
  }
  
  .upload-form {
    padding: 1.5rem;
  }
  
  .file-upload-area {
    padding: 2rem 1rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
