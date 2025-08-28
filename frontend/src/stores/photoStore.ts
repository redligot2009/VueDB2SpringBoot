import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService, type Photo, type PaginatedResponse } from '@/services/api'

export const usePhotoStore = defineStore('photo', () => {
  // State
  const photos = ref<Photo[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Pagination state
  const currentPage = ref(0)
  const pageSize = ref(10)
  const totalElements = ref(0)
  const totalPages = ref(0)

  // Computed properties
  const hasPhotos = computed(() => photos.value.length > 0)
  const photoCount = computed(() => photos.value.length)
  const totalPhotoCount = computed(() => totalElements.value)
  
  // Pagination computed properties
  const hasNextPage = computed(() => currentPage.value < totalPages.value - 1)
  const hasPreviousPage = computed(() => currentPage.value > 0)
  const isFirstPage = computed(() => currentPage.value === 0)
  const isLastPage = computed(() => currentPage.value === totalPages.value - 1)

  // Actions
  const fetchPhotos = async (page: number = 0, size: number = 10) => {
    loading.value = true
    error.value = null
    
    try {
      const response: PaginatedResponse<Photo> = await apiService.getAllPhotos(page, size)
      photos.value = response.content
      currentPage.value = response.number
      pageSize.value = response.size
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch photos'
      console.error('Error fetching photos:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchNextPage = async () => {
    if (hasNextPage.value) {
      await fetchPhotos(currentPage.value + 1, pageSize.value)
    }
  }

  const fetchPreviousPage = async () => {
    if (hasPreviousPage.value) {
      await fetchPhotos(currentPage.value - 1, pageSize.value)
    }
  }

  const goToPage = async (page: number) => {
    if (page >= 0 && page < totalPages.value) {
      await fetchPhotos(page, pageSize.value)
    }
  }

  const addPhoto = async (title: string, description: string, file: File) => {
    // Client-side validation
    if (file.size > 8 * 1024 * 1024) {
      throw new Error('File size exceeds maximum limit of 8MB')
    }
    
    if (!file.type.startsWith('image/')) {
      throw new Error('Please select a valid image file (JPG, PNG, GIF, WebP)')
    }

    loading.value = true
    error.value = null
    
    try {
      const newPhoto = await apiService.createPhoto(title, description, file)
      // Refresh the current page to show the new photo
      await fetchPhotos(currentPage.value, pageSize.value)
      return newPhoto
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to add photo'
      throw err
    } finally {
      loading.value = false
    }
  }

  const updatePhoto = async (id: number, title: string, description: string, file?: File) => {
    // Client-side validation for file if provided
    if (file) {
      if (file.size > 8 * 1024 * 1024) {
        throw new Error('File size exceeds maximum limit of 8MB')
      }
      
      if (!file.type.startsWith('image/')) {
        throw new Error('Please select a valid image file (JPG, PNG, GIF, WebP)')
      }
    }

    loading.value = true
    error.value = null
    
    try {
      const updatedPhoto = await apiService.updatePhoto(id, title, description, file)
      // Update the photo in the current list
      const index = photos.value.findIndex(p => p.id === id)
      if (index !== -1) {
        photos.value[index] = updatedPhoto
      }
      return updatedPhoto
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update photo'
      throw err
    } finally {
      loading.value = false
    }
  }

  const deletePhoto = async (id: number) => {
    loading.value = true
    error.value = null
    
    try {
      await apiService.deletePhoto(id)
      // Remove the photo from the current list
      photos.value = photos.value.filter(p => p.id !== id)
      
      // If we're on the last page and it becomes empty, go to the previous page
      if (photos.value.length === 0 && !isFirstPage.value) {
        await fetchPreviousPage()
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete photo'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // State
    photos,
    loading,
    error,
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    
    // Computed
    hasPhotos,
    photoCount,
    totalPhotoCount,
    hasNextPage,
    hasPreviousPage,
    isFirstPage,
    isLastPage,
    
    // Actions
    fetchPhotos,
    fetchNextPage,
    fetchPreviousPage,
    goToPage,
    addPhoto,
    updatePhoto,
    deletePhoto,
    clearError
  }
})
