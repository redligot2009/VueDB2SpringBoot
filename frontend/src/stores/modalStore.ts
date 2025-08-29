import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface DeleteModalData {
  isOpen: boolean
  message: string
  isDeleting: boolean
  onConfirm: () => Promise<void>
  onCancel?: () => void
}

export const useModalStore = defineStore('modal', () => {
  // State
  const deleteModal = ref<DeleteModalData>({
    isOpen: false,
    message: '',
    isDeleting: false,
    onConfirm: async () => {},
    onCancel: () => {}
  })

  // Actions
  const showDeleteModal = (message: string, onConfirm: () => Promise<void>, onCancel?: () => void) => {
    deleteModal.value = {
      isOpen: true,
      message,
      isDeleting: false,
      onConfirm,
      onCancel
    }
  }

  const hideDeleteModal = () => {
    deleteModal.value.isOpen = false
  }

  const setDeleting = (isDeleting: boolean) => {
    deleteModal.value.isDeleting = isDeleting
  }

  const handleConfirm = async () => {
    setDeleting(true)
    try {
      await deleteModal.value.onConfirm()
      hideDeleteModal()
    } catch (error) {
      console.error('Error in delete confirmation:', error)
    } finally {
      setDeleting(false)
    }
  }

  const handleCancel = () => {
    if (deleteModal.value.onCancel) {
      deleteModal.value.onCancel()
    }
    hideDeleteModal()
  }

  return {
    deleteModal,
    showDeleteModal,
    hideDeleteModal,
    setDeleting,
    handleConfirm,
    handleCancel
  }
})
