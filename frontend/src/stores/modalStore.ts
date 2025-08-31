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
  galleryId?: number
  onUpload: (files: File[], titles?: string[], descriptions?: string[]) => Promise<void>
  onCancel?: () => void
}

export interface MovePhotosModalData {
  isOpen: boolean
  isMoving: boolean
  error: string | null
  photoIds: number[]
  photoCount: number
  availableGalleries: any[]
  onMove: (photoIds: number[], targetGalleryId: number | null) => Promise<void>
  onCancel?: () => void
}

export interface EditGalleryModalData {
  isOpen: boolean
  isUpdating: boolean
  galleryId: number
  currentName: string
  currentDescription: string
  onUpdate: (galleryId: number, name: string, description: string) => Promise<void>
  onCancel?: () => void
}

export interface DeleteGalleryModalData {
  isOpen: boolean
  isDeleting: boolean
  galleryId: number
  galleryName: string
  deleteOption: string
  onDelete: (galleryId: number, deleteOption: string) => Promise<void>
  onCancel?: () => void
}

export interface CreateGalleryModalData {
  isOpen: boolean
  isCreating: boolean
  error: string | null
  onCreate: (name: string, description: string) => Promise<void>
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
    galleryId: undefined,
    onUpload: async () => {},
    onCancel: () => {}
  })

  const movePhotosModal = ref<MovePhotosModalData>({
  isOpen: false,
  isMoving: false,
  error: null,
  photoIds: [],
  photoCount: 0,
  availableGalleries: [],
  onMove: async () => {},
  onCancel: () => {}
})

const editGalleryModal = ref<EditGalleryModalData>({
  isOpen: false,
  isUpdating: false,
  galleryId: 0,
  currentName: '',
  currentDescription: '',
  onUpdate: async () => {},
  onCancel: () => {}
})

const deleteGalleryModal = ref<DeleteGalleryModalData>({
  isOpen: false,
  isDeleting: false,
  galleryId: 0,
  galleryName: '',
  deleteOption: 'move_photos',
  onDelete: async () => {},
  onCancel: () => {}
})

const createGalleryModal = ref<CreateGalleryModalData>({
  isOpen: false,
  isCreating: false,
  error: null,
  onCreate: async () => {},
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
  const showBulkUploadModal = (onUpload: (files: File[], titles?: string[], descriptions?: string[]) => Promise<void>, onCancel?: () => void, galleryId?: number) => {
    bulkUploadModal.value = {
      isOpen: true,
      isUploading: false,
      galleryId,
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

  // Move Photos Modal Actions
  const showMovePhotosModal = (photoIds: number[], availableGalleries: any[], onMove: (photoIds: number[], targetGalleryId: number | null) => Promise<void>, onCancel?: () => void) => {
    movePhotosModal.value = {
      isOpen: true,
      isMoving: false,
      error: null,
      photoIds,
      photoCount: photoIds.length,
      availableGalleries,
      onMove,
      onCancel
    }
  }

  const hideMovePhotosModal = () => {
    movePhotosModal.value.isOpen = false
  }

  const setMoving = (isMoving: boolean) => {
    movePhotosModal.value.isMoving = isMoving
  }

  const handleMovePhotos = async (targetGalleryId: number | null) => {
    setMoving(true)
    // Clear any previous errors
    movePhotosModal.value.error = null
    try {
      await movePhotosModal.value.onMove(movePhotosModal.value.photoIds, targetGalleryId)
      hideMovePhotosModal()
    } catch (error) {
      console.error('Error in move photos:', error)
      // Set error message for the user
      movePhotosModal.value.error = error instanceof Error ? error.message : 'Failed to move photos'
    } finally {
      setMoving(false)
    }
  }

  const handleMovePhotosCancel = () => {
  if (movePhotosModal.value.onCancel) {
    movePhotosModal.value.onCancel()
  }
  hideMovePhotosModal()
}

const showEditGalleryModal = (galleryId: number, currentName: string, currentDescription: string, onUpdate: (galleryId: number, name: string, description: string) => Promise<void>, onCancel?: () => void) => {
  editGalleryModal.value = {
    isOpen: true,
    isUpdating: false,
    galleryId,
    currentName,
    currentDescription,
    onUpdate,
    onCancel
  }
}

const hideEditGalleryModal = () => {
  editGalleryModal.value.isOpen = false
}

const setUpdating = (isUpdating: boolean) => {
  editGalleryModal.value.isUpdating = isUpdating
}

const handleEditGallery = async (name: string, description: string) => {
  setUpdating(true)
  try {
    await editGalleryModal.value.onUpdate(editGalleryModal.value.galleryId, name, description)
    hideEditGalleryModal()
  } catch (error) {
    console.error('Error in edit gallery:', error)
  } finally {
    setUpdating(false)
  }
}

const handleEditGalleryCancel = () => {
  if (editGalleryModal.value.onCancel) {
    editGalleryModal.value.onCancel()
  }
  hideEditGalleryModal()
}

const showDeleteGalleryModal = (galleryId: number, galleryName: string, onDelete: (galleryId: number, deleteOption: string) => Promise<void>, onCancel?: () => void) => {
  deleteGalleryModal.value = {
    isOpen: true,
    isDeleting: false,
    galleryId,
    galleryName,
    deleteOption: 'move_photos',
    onDelete,
    onCancel
  }
}

const hideDeleteGalleryModal = () => {
  deleteGalleryModal.value.isOpen = false
}

const setDeletingGallery = (isDeleting: boolean) => {
  deleteGalleryModal.value.isDeleting = isDeleting
}

const handleDeleteGallery = async () => {
  setDeletingGallery(true)
  try {
    await deleteGalleryModal.value.onDelete(deleteGalleryModal.value.galleryId, deleteGalleryModal.value.deleteOption)
    hideDeleteGalleryModal()
  } catch (error) {
    console.error('Error in delete gallery:', error)
  } finally {
    setDeletingGallery(false)
  }
}

const handleDeleteGalleryCancel = () => {
  if (deleteGalleryModal.value.onCancel) {
    deleteGalleryModal.value.onCancel()
  }
  hideDeleteGalleryModal()
}

const setDeleteGalleryOption = (option: string) => {
  deleteGalleryModal.value.deleteOption = option
}

// Create Gallery Modal Actions
const showCreateGalleryModal = (onCreate: (name: string, description: string) => Promise<void>, onCancel?: () => void) => {
  createGalleryModal.value = {
    isOpen: true,
    isCreating: false,
    error: null,
    onCreate,
    onCancel
  }
}

const hideCreateGalleryModal = () => {
  createGalleryModal.value.isOpen = false
}

const setCreating = (isCreating: boolean) => {
  createGalleryModal.value.isCreating = isCreating
}

const handleCreateGallery = async (name: string, description: string) => {
  setCreating(true)
  // Clear any previous errors
  createGalleryModal.value.error = null
  try {
    await createGalleryModal.value.onCreate(name, description)
    hideCreateGalleryModal()
  } catch (error) {
    console.error('Error in create gallery:', error)
    // Set error message for the user
    createGalleryModal.value.error = error instanceof Error ? error.message : 'Failed to create gallery'
  } finally {
    setCreating(false)
  }
}

const handleCreateGalleryCancel = () => {
  if (createGalleryModal.value.onCancel) {
    createGalleryModal.value.onCancel()
  }
  hideCreateGalleryModal()
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
    handleBulkUploadCancel,
    movePhotosModal,
    showMovePhotosModal,
    hideMovePhotosModal,
    setMoving,
    handleMovePhotos,
    handleMovePhotosCancel,
    editGalleryModal,
    showEditGalleryModal,
    hideEditGalleryModal,
    setUpdating,
    handleEditGallery,
    handleEditGalleryCancel,
    deleteGalleryModal,
    showDeleteGalleryModal,
    hideDeleteGalleryModal,
    setDeletingGallery,
    handleDeleteGallery,
    handleDeleteGalleryCancel,
    setDeleteGalleryOption,
    createGalleryModal,
    showCreateGalleryModal,
    hideCreateGalleryModal,
    setCreating,
    handleCreateGallery,
    handleCreateGalleryCancel
  }
})
