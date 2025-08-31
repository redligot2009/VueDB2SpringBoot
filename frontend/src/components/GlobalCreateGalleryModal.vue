<template>
  <div v-if="modalStore.createGalleryModal.isOpen" class="modal-overlay" @click="modalStore.handleCreateGalleryCancel">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h3>Create New Gallery</h3>
        <button @click="modalStore.handleCreateGalleryCancel" class="modal-close">
          <font-awesome-icon icon="fa-solid fa-times" />
        </button>
      </div>
      <div class="modal-body">
        <!-- Error Message -->
        <div v-if="modalStore.createGalleryModal.error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ modalStore.createGalleryModal.error }}
        </div>
        
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label for="galleryName">Gallery Name *</label>
            <input 
              id="galleryName"
              v-model="formData.name"
              type="text"
              required
              maxlength="100"
              placeholder="Enter gallery name"
              class="form-control"
              :disabled="modalStore.createGalleryModal.isCreating"
            />
          </div>
          
          <div class="form-group">
            <label for="galleryDescription">Description (optional)</label>
            <textarea 
              id="galleryDescription"
              v-model="formData.description"
              maxlength="500"
              placeholder="Enter gallery description"
              class="form-control"
              rows="3"
              :disabled="modalStore.createGalleryModal.isCreating"
            ></textarea>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="modalStore.handleCreateGalleryCancel" class="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" :disabled="!canSubmit || modalStore.createGalleryModal.isCreating" class="btn btn-primary">
              <font-awesome-icon v-if="modalStore.createGalleryModal.isCreating" icon="fa-solid fa-spinner" spin />
              {{ modalStore.createGalleryModal.isCreating ? 'Creating...' : 'Create Gallery' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useModalStore } from '@/stores/modalStore'

const modalStore = useModalStore()

const formData = ref({
  name: '',
  description: ''
})

const canSubmit = computed(() => {
  return formData.value.name.trim().length > 0 && !modalStore.createGalleryModal.isCreating
})

const handleSubmit = () => {
  if (!canSubmit.value) return
  
  modalStore.handleCreateGallery(
    formData.value.name.trim(),
    formData.value.description.trim()
  )
  
  // Reset form
  formData.value = { name: '', description: '' }
}
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
}

.modal {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 1rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  padding: 0.75rem;
  margin-bottom: 1rem;
  color: #dc2626;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.error-message i {
  color: #dc2626;
  font-size: 1rem;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
  background-color: white;
  resize: vertical;
}

.form-control:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-control:disabled {
  background-color: #f9fafb;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #4b5563;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

@media (max-width: 640px) {
  .modal {
    width: 95%;
    margin: 1rem;
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
