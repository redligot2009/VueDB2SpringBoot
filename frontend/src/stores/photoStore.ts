import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService, type Photo, type PaginatedResponse } from '@/services/api'
import { useAuthStore } from './authStore'

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
  
  // Gallery filtering state
  const currentGalleryId = ref<number | undefined>(undefined)

  // Computed properties
  const hasPhotos = computed(() => {
    const hasPhotosValue = photos.value?.length > 0
    console.log('🔍 hasPhotos computed - photos length:', photos.value?.length, 'hasPhotos:', hasPhotosValue)
    return hasPhotosValue
  })
  const photoCount = computed(() => photos.value?.length || 0)
  const totalPhotoCount = computed(() => totalElements.value)
  
  // Pagination computed properties
  const hasNextPage = computed(() => currentPage.value < totalPages.value - 1)
  const hasPreviousPage = computed(() => currentPage.value > 0)
  const isFirstPage = computed(() => currentPage.value === 0)
  const isLastPage = computed(() => currentPage.value === totalPages.value - 1)

  // Actions
  const fetchPhotos = async (page: number = 0, size: number = 10, galleryId?: number) => {
    // Check if user is authenticated before making API call
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      console.warn('User not authenticated, skipping photo fetch')
      return
    }
    
    console.log('🔍 Fetching photos with authentication...')
    console.log('Auth headers:', authStore.getAuthHeaders())
    console.log('User ID:', authStore.getCurrentUserId())
    console.log('Gallery ID:', galleryId)
    
    loading.value = true
    error.value = null
    
    try {
      const response: PaginatedResponse<Photo> = await apiService.getAllPhotos(page, size, galleryId)
      console.log('✅ Photos fetched successfully:', response)
      console.log('📊 Response type:', typeof response)
      
      // Response should now be a clean object from the backend
      const parsedResponse = response
      
      console.log('📊 Response keys:', Object.keys(parsedResponse || {}))
      console.log('📊 Response content length:', parsedResponse?.content?.length)
      console.log('📊 Response content:', parsedResponse?.content)
      
      // Ensure we're accessing the correct properties
      const content = parsedResponse?.content || []
      const responseNumber = parsedResponse?.number || 0
      const responseSize = parsedResponse?.size || 10
      const responseTotalElements = parsedResponse?.totalElements || 0
      const responseTotalPages = parsedResponse?.totalPages || 0
      
      console.log('📊 Extracted content:', content)
      console.log('📊 Extracted content length:', content?.length)
      
      photos.value = content
      currentPage.value = responseNumber
      pageSize.value = responseSize
      totalElements.value = responseTotalElements
      totalPages.value = responseTotalPages
      currentGalleryId.value = galleryId
      console.log('📊 Photos store updated:', photos.value)
      console.log('📊 Has photos:', photos.value?.length > 0)
    } catch (err) {
      console.error('❌ Error fetching photos:', err)
      error.value = err instanceof Error ? err.message : 'Failed to fetch photos'
      // Reset to safe defaults on error
      photos.value = []
      currentPage.value = 0
      pageSize.value = 10
      totalElements.value = 0
      totalPages.value = 0
    } finally {
      loading.value = false
    }
  }

  const fetchNextPage = async () => {
    if (hasNextPage.value) {
      await fetchPhotos(currentPage.value + 1, pageSize.value, currentGalleryId.value)
    }
  }

  const fetchPreviousPage = async () => {
    if (hasPreviousPage.value) {
      await fetchPhotos(currentPage.value - 1, pageSize.value, currentGalleryId.value)
    }
  }

  const goToPage = async (page: number) => {
    if (page >= 0 && page < totalPages.value) {
      await fetchPhotos(page, pageSize.value, currentGalleryId.value)
    }
  }

  const addPhoto = async (title: string, description: string, file: File, galleryId?: number) => {
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
      const newPhoto = await apiService.createPhoto(title, description, file, galleryId)
      // Refresh the current page to show the new photo
      await fetchPhotos(currentPage.value, pageSize.value, currentGalleryId.value)
      return newPhoto
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to add photo'
      throw err
    } finally {
      loading.value = false
    }
  }

  const bulkAddPhotos = async (files: File[], titles?: string[], descriptions?: string[], galleryId?: number) => {
    // Client-side validation for all files
    for (const file of files) {
      if (file.size > 8 * 1024 * 1024) {
        throw new Error(`File ${file.name} exceeds maximum limit of 8MB`)
      }
      
      if (!file.type.startsWith('image/')) {
        throw new Error(`File ${file.name} is not a valid image file (JPG, PNG, GIF, WebP)`)
      }
    }

    loading.value = true
    error.value = null
    
    try {
      const newPhotos = await apiService.bulkCreatePhotos(files, titles, descriptions, galleryId)
      // Refresh the current page to show the new photos
      await fetchPhotos(currentPage.value, pageSize.value, currentGalleryId.value)
      return newPhotos
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to add photos'
      throw err
    } finally {
      loading.value = false
    }
  }

  const updatePhoto = async (id: number, title: string, description: string, file?: File, galleryId?: number) => {
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
      const updatedPhoto = await apiService.updatePhoto(id, title, description, file, galleryId)
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

  const bulkDeletePhotos = async (ids: number[]) => {
    loading.value = true
    error.value = null
    
    try {
      await apiService.bulkDeletePhotos(ids)
      // Remove the deleted photos from the current list
      photos.value = photos.value.filter(p => !ids.includes(p.id))
      
      // If we're on the last page and it becomes empty, go to the previous page
      if (photos.value.length === 0 && !isFirstPage.value) {
        await fetchPreviousPage()
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete photos'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  const clearStore = () => {
    photos.value = []
    loading.value = false
    error.value = null
    currentPage.value = 0
    pageSize.value = 10
    totalElements.value = 0
    totalPages.value = 0
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
    bulkAddPhotos,
    updatePhoto,
    deletePhoto,
    bulkDeletePhotos,
    clearError,
    clearStore
  }
})
