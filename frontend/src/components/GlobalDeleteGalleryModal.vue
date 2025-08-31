<template>
  <div v-if="modalStore.deleteGalleryModal.isOpen" class="modal-overlay" @click="modalStore.handleDeleteGalleryCancel">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h3>Delete Gallery</h3>
        <button @click="modalStore.handleDeleteGalleryCancel" class="modal-close">
          <font-awesome-icon icon="fa-solid fa-times" />
        </button>
      </div>
      <div class="modal-body">
        <div class="warning-message">
          <i class="fas fa-exclamation-triangle"></i>
          <h4>Are you sure you want to delete "{{ modalStore.deleteGalleryModal.galleryName }}"?</h4>
          <p>This action cannot be undone.</p>
        </div>
        
        <div class="form-group">
          <label class="radio-label">
            <input
              type="radio"
              v-model="deleteOption"
              value="delete_photos"
              name="delete_option"
            />
            <span>Delete all photos in this gallery</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              v-model="deleteOption"
              value="move_photos"
              name="delete_option"
            />
            <span>Move photos to Unorganized Photos</span>
          </label>
        </div>

        <div class="form-actions">
          <button type="button" @click="modalStore.handleDeleteGalleryCancel" class="btn btn-secondary">
            Cancel
          </button>
          <button @click="handleDelete" :disabled="modalStore.deleteGalleryModal.isDeleting" class="btn btn-danger">
            <font-awesome-icon v-if="modalStore.deleteGalleryModal.isDeleting" icon="fa-solid fa-spinner" spin />
            {{ modalStore.deleteGalleryModal.isDeleting ? 'Deleting...' : 'Delete Gallery' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useModalStore } from '@/stores/modalStore'

const modalStore = useModalStore()
const deleteOption = ref('move_photos')

// Watch for modal data changes and update form
watch(() => modalStore.deleteGalleryModal.deleteOption, (newOption) => {
  deleteOption.value = newOption
})

// Watch for form changes and update modal store
watch(deleteOption, (newOption) => {
  modalStore.setDeleteGalleryOption(newOption)
})

const handleDelete = () => {
  modalStore.handleDeleteGallery()
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
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
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

.warning-message {
  text-align: center;
  padding: 1rem;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 6px;
  margin-bottom: 1.5rem;
}

.warning-message i {
  font-size: 2rem;
  color: #f39c12;
  margin-bottom: 0.5rem;
}

.warning-message h4 {
  margin: 0 0 0.5rem 0;
  color: #856404;
}

.warning-message p {
  margin: 0;
  color: #856404;
}

.form-group {
  margin-bottom: 1.5rem;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.radio-label:hover {
  background: #f8f9fa;
}

.radio-label input[type="radio"] {
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
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

.btn-danger {
  background: #dc2626;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
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
    justify-content: center;
  }
}
</style>
