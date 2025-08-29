import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface DeleteModalData {
  isOpen: boolean
  message: string
  isDeleting: boolean
  onConfirm: () => Promise<void>
  onCancel?: () => void
}

export interface BulkUploadModalData {
  isOpen: boolean
  isUploading: boolean
  onUpload: (files: File[], titles?: string[], descriptions?: string[]) => Promise<void>
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

  const bulkUploadModal = ref<BulkUploadModalData>({
    isOpen: false,
    isUploading: false,
    onUpload: async () => {},
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

  // Bulk Upload Modal Actions
  const showBulkUploadModal = (onUpload: (files: File[], titles?: string[], descriptions?: string[]) => Promise<void>, onCancel?: () => void) => {
    bulkUploadModal.value = {
      isOpen: true,
      isUploading: false,
      onUpload,
      onCancel
    }
  }

  const hideBulkUploadModal = () => {
    bulkUploadModal.value.isOpen = false
  }

  const setBulkUploading = (isUploading: boolean) => {
    bulkUploadModal.value.isUploading = isUploading
  }

  const handleBulkUpload = async (files: File[], titles?: string[], descriptions?: string[]) => {
    setBulkUploading(true)
    try {
      await bulkUploadModal.value.onUpload(files, titles, descriptions)
      hideBulkUploadModal()
    } catch (error) {
      console.error('Error in bulk upload:', error)
    } finally {
      setBulkUploading(false)
    }
  }

  const handleBulkUploadCancel = () => {
    if (bulkUploadModal.value.onCancel) {
      bulkUploadModal.value.onCancel()
    }
    hideBulkUploadModal()
  }

  return {
    deleteModal,
    showDeleteModal,
    hideDeleteModal,
    setDeleting,
    handleConfirm,
    handleCancel,
    bulkUploadModal,
    showBulkUploadModal,
    hideBulkUploadModal,
    setBulkUploading,
    handleBulkUpload,
    handleBulkUploadCancel
  }
})
