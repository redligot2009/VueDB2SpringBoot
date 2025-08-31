<template>
  <div v-if="modalStore.editGalleryModal.isOpen" class="modal-overlay" @click="modalStore.handleEditGalleryCancel">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h3>Edit Gallery</h3>
        <button @click="modalStore.handleEditGalleryCancel" class="modal-close">
          <font-awesome-icon icon="fa-solid fa-times" />
        </button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="gallery-name">Gallery Name</label>
            <input
              id="gallery-name"
              v-model="formData.name"
              type="text"
              required
              maxlength="100"
              class="form-control"
              placeholder="Enter gallery name"
            />
          </div>
          <div class="form-group">
            <label for="gallery-description">Description (Optional)</label>
            <textarea
              id="gallery-description"
              v-model="formData.description"
              rows="3"
              maxlength="500"
              class="form-control"
              placeholder="Enter gallery description"
            ></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="modalStore.handleEditGalleryCancel" class="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" :disabled="modalStore.editGalleryModal.isUpdating" class="btn btn-primary">
              <font-awesome-icon v-if="modalStore.editGalleryModal.isUpdating" icon="fa-solid fa-spinner" spin />
              {{ modalStore.editGalleryModal.isUpdating ? 'Updating...' : 'Update Gallery' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useModalStore } from '@/stores/modalStore'

const modalStore = useModalStore()

const formData = ref({
  name: '',
  description: ''
})

// Watch for modal data changes and update form
watch(() => modalStore.editGalleryModal.currentName, (newName) => {
  formData.value.name = newName
})

watch(() => modalStore.editGalleryModal.currentDescription, (newDescription) => {
  formData.value.description = newDescription
})

const handleSubmit = () => {
  modalStore.handleEditGallery(formData.value.name, formData.value.description)
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

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-control::placeholder {
  color: #9ca3af;
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

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
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
