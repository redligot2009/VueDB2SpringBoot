<template>
  <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">Bulk Upload Photos</h3>
        <button @click="handleCancel" class="close-btn" aria-label="Close">
          ✕
        </button>
      </div>
      
      <div class="modal-body">
        <!-- File Upload Area -->
        <div class="upload-section">
          <div 
            class="file-upload-area" 
            :class="{ 
              'drag-over': isDragOver, 
              'has-files': selectedFiles.length > 0 
            }"
            @drop="handleDrop"
            @dragover.prevent="isDragOver = true"
            @dragleave.prevent="isDragOver = false"
            @dragenter.prevent
          >
            <input 
              ref="fileInput" 
              type="file" 
              multiple
              accept="image/*" 
              @change="handleFileSelect" 
              class="file-input"
            />
            <div v-if="selectedFiles.length === 0" class="upload-placeholder">
              <span>📁 Click to browse or drag and drop multiple images</span>
              <small>Maximum file size: 8MB per file • Supported formats: JPG, PNG, GIF, WebP</small>
            </div>
            <div v-else class="files-selected">
              <span>✅ {{ selectedFiles.length }} file(s) selected</span>
              <small>{{ getTotalFileSize() }}</small>
            </div>
            <button type="button" @click="triggerFileInput" class="browse-btn">
              Browse Files
            </button>
          </div>
        </div>

        <!-- File List -->
        <div v-if="selectedFiles.length > 0" class="files-section">
          <h4>Selected Files</h4>
          <div class="files-list">
            <div 
              v-for="(file, index) in selectedFiles" 
              :key="index"
              class="file-item"
              :class="{ 'file-error': getFileError(file) }"
            >
              <div class="file-info">
                <div class="file-preview">
                  <img 
                    v-if="file.type.startsWith('image/')" 
                    :src="getFilePreview(file)" 
                    :alt="file.name"
                    class="preview-image"
                  />
                  <div v-else class="preview-placeholder">
                    📄
                  </div>
                </div>
                <div class="file-details">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-size">{{ formatFileSize(file.size) }}</div>
                  <div v-if="getFileError(file)" class="file-error-message">
                    {{ getFileError(file) }}
                  </div>
                </div>
              </div>
              <div class="file-actions">
                <input 
                  v-model="fileTitles[index]" 
                  :placeholder="getDefaultTitle(file)"
                  class="title-input"
                />
                <textarea 
                  v-model="fileDescriptions[index]" 
                  placeholder="Description (optional)"
                  class="description-input"
                  rows="2"
                ></textarea>
                <button @click="removeFile(index)" class="remove-btn" type="button">
                  ✕
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Error Messages -->
        <div v-if="error" class="error-message">
          <p>{{ error }}</p>
        </div>
      </div>
      
      <div class="modal-actions">
        <button @click="handleCancel" class="cancel-btn" :disabled="isUploading">
          Cancel
        </button>
        <button 
          @click="handleUpload" 
          class="upload-btn" 
          :disabled="!canUpload || isUploading"
        >
          <span v-if="isUploading">📤 Uploading...</span>
          <span v-else>📤 Upload {{ selectedFiles.length }} Photo{{ selectedFiles.length !== 1 ? 's' : '' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  isOpen: boolean
  isUploading: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  upload: [files: File[], titles?: string[], descriptions?: string[]]
  cancel: []
}>()

// State
const fileInput = ref<HTMLInputElement>()
const selectedFiles = ref<File[]>([])
const fileTitles = ref<string[]>([])
const fileDescriptions = ref<string[]>([])
const isDragOver = ref(false)
const error = ref<string | null>(null)

// Computed
const canUpload = computed(() => {
  return selectedFiles.value.length > 0 && 
         !selectedFiles.value.some(file => getFileError(file)) &&
         !props.isUploading
})

// Methods
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    addFiles(Array.from(target.files))
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (event.dataTransfer?.files) {
    addFiles(Array.from(event.dataTransfer.files))
  }
}

const addFiles = (files: File[]) => {
  const imageFiles = files.filter(file => file.type.startsWith('image/'))
  
  // Add new files to the list
  selectedFiles.value.push(...imageFiles)
  
  // Initialize titles and descriptions for new files
  const startIndex = fileTitles.value.length
  for (let i = 0; i < imageFiles.length; i++) {
    const index = startIndex + i
    fileTitles.value[index] = getDefaultTitle(imageFiles[i])
    fileDescriptions.value[index] = ''
  }
}

const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
  fileTitles.value.splice(index, 1)
  fileDescriptions.value.splice(index, 1)
}

const getDefaultTitle = (file: File): string => {
  const name = file.name
  const lastDotIndex = name.lastIndexOf('.')
  return lastDotIndex > 0 ? name.substring(0, lastDotIndex) : name
}

const getFileError = (file: File): string | null => {
  if (file.size > 8 * 1024 * 1024) {
    return 'File size exceeds 8MB limit'
  }
  if (!file.type.startsWith('image/')) {
    return 'Not a valid image file'
  }
  return null
}

const getFilePreview = (file: File): string => {
  return URL.createObjectURL(file)
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getTotalFileSize = (): string => {
  const totalBytes = selectedFiles.value.reduce((sum, file) => sum + file.size, 0)
  return `Total size: ${formatFileSize(totalBytes)}`
}

const handleUpload = async () => {
  if (!canUpload.value) return

  error.value = null

  try {
    // Debug: Log what we're about to send
    console.log('BulkUploadModal - Files to upload:', selectedFiles.value.length)
    selectedFiles.value.forEach((file, index) => {
      console.log(`File ${index}:`, file.name, file.type, file.size)
    })
    
    const titles = fileTitles.value.map(title => title.trim()).filter(title => title.length > 0)
    const descriptions = fileDescriptions.value.map(desc => desc.trim()).filter(desc => desc.length > 0)
    
    console.log('Titles to send:', titles)
    console.log('Descriptions to send:', descriptions)
    
    // Emit the upload event with the data
    emit('upload', 
      selectedFiles.value,
      titles.length > 0 ? titles : undefined,
      descriptions.length > 0 ? descriptions : undefined
    )
  } catch (err) {
    console.error('BulkUploadModal - Upload error:', err)
    error.value = err instanceof Error ? err.message : 'Failed to upload photos'
  }
}

const handleCancel = () => {
  selectedFiles.value = []
  fileTitles.value = []
  fileDescriptions.value = []
  error.value = null
  emit('cancel')
}

const handleOverlayClick = () => {
  if (!props.isUploading) {
    handleCancel()
  }
}

// Watch for modal open/close
watch(() => props.isOpen, (isOpen) => {
  if (!isOpen) {
    handleCancel()
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0 1.5rem;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.upload-section {
  margin-bottom: 1.5rem;
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

.file-upload-area.has-files {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.file-input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.upload-placeholder,
.files-selected {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #6b7280;
}

.upload-placeholder span,
.files-selected span {
  font-size: 1rem;
  font-weight: 500;
}

.upload-placeholder small,
.files-selected small {
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

.files-section {
  margin-top: 1.5rem;
}

.files-section h4 {
  margin: 0 0 1rem 0;
  color: #1a1a1a;
  font-weight: 600;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.file-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  background: #f9fafb;
}

.file-item.file-error {
  border-color: #fecaca;
  background: #fef2f2;
}

.file-info {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.file-preview {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #f3f4f6;
  flex-shrink: 0;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #6b7280;
}

.preview-placeholder i {
  font-size: 1.5rem;
}

.file-details {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 0.25rem;
}

.file-size {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.25rem;
}

.file-error-message {
  font-size: 0.875rem;
  color: #dc2626;
}

.file-actions {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.title-input,
.description-input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.875rem;
  resize: vertical;
}

.title-input:focus,
.description-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.remove-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.remove-btn:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  padding: 0.75rem;
  margin-top: 1rem;
  color: #dc2626;
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  padding: 0 1.5rem 1.5rem 1.5rem;
}

.cancel-btn {
  flex: 1;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.cancel-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-btn {
  flex: 1;
  background: #2563eb;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.upload-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .modal-header,
  .modal-body,
  .modal-actions {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .file-info {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .file-actions {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>
