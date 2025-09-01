<template>
  <div v-if="modalStore.bulkUploadModal.isOpen" class="modal-overlay" @click="handleClose">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h3>{{ modalTitle }}</h3>
        <button @click="handleClose" class="modal-close">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="modal-body">
        <div class="upload-area" @click="triggerFileInput" @drop="handleDrop" @dragover.prevent>
          <input
            ref="fileInput"
            type="file"
            multiple
            accept="image/*"
            @change="handleFileSelect"
            style="display: none"
          />
          <div class="upload-content">
            <i class="fas fa-cloud-upload-alt"></i>
            <h4>Drop photos here or click to select</h4>
            <p>Supports JPG, PNG, GIF, and WebP formats</p>
          </div>
        </div>

        <!-- Photo Details Form -->
        <div v-if="selectedFiles.length > 0" class="photo-details">
          <h4>Photo Details</h4>
          <div v-for="(file, index) in selectedFiles" :key="index" class="photo-detail-item">
            <div class="photo-preview">
              <img :src="file.preview" :alt="file.name" />
              <span class="file-name">{{ file.name }}</span>
            </div>
            <div class="photo-fields">
              <div class="form-group">
                <label :for="`title-${index}`">Title</label>
                <input
                  :id="`title-${index}`"
                  v-model="file.title"
                  type="text"
                  :placeholder="getDefaultTitle(file.name)"
                  class="form-control"
                />
              </div>
              <div class="form-group">
                <label :for="`description-${index}`">Description (Optional)</label>
                <textarea
                  :id="`description-${index}`"
                  v-model="file.description"
                  rows="2"
                  placeholder="Add a description..."
                  class="form-control"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="uploadingFiles.length > 0" class="upload-progress">
          <h4>Uploading {{ uploadingFiles.length }} photo{{ uploadingFiles.length > 1 ? 's' : '' }}...</h4>
          <div v-for="file in uploadingFiles" :key="file.name" class="upload-item">
            <span>{{ file.name }}</span>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: file.progress + '%' }"></div>
            </div>
            <span v-if="file.progress === 100" class="success-icon">
              <i class="fas fa-check"></i>
            </span>
            <span v-else-if="file.progress === -1" class="error-icon">
              <i class="fas fa-times"></i>
            </span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="handleClose" class="btn btn-secondary">
            Cancel
          </button>
          <button 
            v-if="selectedFiles.length > 0 && uploadingFiles.length === 0" 
            @click="uploadFiles" 
            class="btn btn-primary"
            :disabled="modalStore.bulkUploadModal.isUploading"
          >
            <i class="fas fa-upload"></i> Upload {{ selectedFiles.length }} Photo{{ selectedFiles.length > 1 ? 's' : '' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useModalStore } from '@/stores/modalStore'

const modalStore = useModalStore()
const fileInput = ref<HTMLInputElement>()
const uploadingFiles = ref<Array<{ name: string; progress: number }>>([])
const selectedFiles = ref<Array<{
  file: File
  name: string
  preview: string
  title: string
  description: string
}>>([])

const modalTitle = computed(() => {
  const galleryId = modalStore.bulkUploadModal.galleryId
  if (galleryId) {
    return `Upload Photos to Gallery`
  }
  return 'Upload Photos'
})

const handleClose = () => {
  modalStore.hideBulkUploadModal()
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    handleFiles(Array.from(target.files))
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer?.files) {
    handleFiles(Array.from(event.dataTransfer.files))
  }
}

const getDefaultTitle = (filename: string) => {
  if (!filename) return 'Untitled'
  const lastDotIndex = filename.lastIndexOf('.')
  return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename
}

const createFilePreview = (file: File): Promise<string> => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      resolve(e.target?.result as string)
    }
    reader.readAsDataURL(file)
  })
}

const handleFiles = async (files: File[]) => {
  const imageFiles = files.filter(file => file.type.startsWith('image/'))
  
  if (imageFiles.length === 0) {
    alert('Please select image files only.')
    return
  }

  // Check file count limit (reasonable limit for bulk upload)
  const MAX_FILES = 20
  if (imageFiles.length > MAX_FILES) {
    alert(`Too many files selected. Please select no more than ${MAX_FILES} files at once.`)
    return
  }

  // Check total size limit (approximate - 100MB total)
  const MAX_TOTAL_SIZE = 100 * 1024 * 1024 // 100MB
  const totalSize = imageFiles.reduce((sum, file) => sum + file.size, 0)
  if (totalSize > MAX_TOTAL_SIZE) {
    alert(`Total file size too large. Please select files with a total size under 100MB.`)
    return
  }

  // Create file objects with previews and default titles
  const fileObjects = await Promise.all(
    imageFiles.map(async (file) => ({
      file,
      name: file.name,
      preview: await createFilePreview(file),
      title: getDefaultTitle(file.name),
      description: ''
    }))
  )

  selectedFiles.value = fileObjects
}

const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return

  uploadingFiles.value = selectedFiles.value.map(file => ({
    name: file.name,
    progress: 0
  }))

  try {
    const files = selectedFiles.value.map(f => f.file)
    const titles = selectedFiles.value.map(f => f.title)
    const descriptions = selectedFiles.value.map(f => f.description)

    // Use the modal store's upload handler
    await modalStore.bulkUploadModal.onUpload(files, titles, descriptions)
    
    // Mark all files as completed
    uploadingFiles.value.forEach(file => {
      file.progress = 100
    })
    
    // Close modal after a short delay
    setTimeout(() => {
      handleClose()
      uploadingFiles.value = []
      selectedFiles.value = []
    }, 1000)
    
  } catch (err: any) {
    console.error('Error uploading files:', err)
    // Mark all files as failed
    uploadingFiles.value.forEach(file => {
      file.progress = -1
    })
  }
}

// Reset upload files when modal closes
watch(() => modalStore.bulkUploadModal.isOpen, (newValue) => {
  if (!newValue) {
    uploadingFiles.value = []
    selectedFiles.value = []
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.modal-close:hover {
  background: #f8f9fa;
}

.modal-body {
  padding: 1.5rem;
}

.upload-area {
  border: 2px dashed #ced4da;
  border-radius: 8px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 1.5rem;
}

.upload-area:hover {
  border-color: #007bff;
  background: #f8f9fa;
}

.upload-content i {
  font-size: 3rem;
  color: #6c757d;
  margin-bottom: 1rem;
}

.upload-content h4 {
  margin: 0 0 0.5rem 0;
  color: #495057;
}

.upload-content p {
  margin: 0;
  color: #6c757d;
}

.upload-progress {
  margin-top: 1.5rem;
}

.upload-progress h4 {
  margin: 0 0 1rem 0;
  color: #495057;
}

.upload-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.upload-item span {
  flex: 1;
  font-size: 0.9rem;
  color: #495057;
}

.progress-bar {
  width: 100px;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #007bff;
  transition: width 0.3s ease;
}

.success-icon {
  color: #28a745;
  font-size: 1rem;
}

.error-icon {
  color: #dc3545;
  font-size: 1rem;
}

.photo-details {
  margin-top: 1.5rem;
  border-top: 1px solid #e1e5e9;
  padding-top: 1.5rem;
}

.photo-details h4 {
  margin: 0 0 1rem 0;
  color: #495057;
}

.photo-detail-item {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e1e5e9;
}

.photo-preview {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.photo-preview img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dee2e6;
}

.file-name {
  font-size: 0.8rem;
  color: #6c757d;
  text-align: center;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.photo-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #495057;
  font-size: 0.9rem;
}

.form-control {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-size: 0.9rem;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

/* Responsive Design */
@media (max-width: 768px) {
  .modal {
    margin: 1rem;
    max-width: none;
  }
  
  .photo-detail-item {
    flex-direction: column;
    gap: 1rem;
  }
  
  .photo-preview {
    align-self: center;
  }
  
  .photo-fields {
    width: 100%;
  }
}
</style>
