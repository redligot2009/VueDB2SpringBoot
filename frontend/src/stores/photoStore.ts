import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService, type Photo, type PhotoMetadata } from '@/services/api'

export const usePhotoStore = defineStore('photo', () => {
  const photos = ref<Photo[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Computed properties
  const photoCount = computed(() => photos.value.length)
  const hasPhotos = computed(() => photos.value.length > 0)

  // Actions
  const fetchPhotos = async () => {
    loading.value = true
    error.value = null
    try {
      photos.value = await apiService.getAllPhotos()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch photos'
      console.error('Error fetching photos:', err)
    } finally {
      loading.value = false
    }
  }

  const addPhoto = async (title: string, description: string, file: File) => {
    loading.value = true
    error.value = null
    try {
      const newPhoto = await apiService.createPhoto(title, description, file)
      photos.value.push(newPhoto)
      return newPhoto
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to create photo'
      console.error('Error creating photo:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updatePhoto = async (id: number, title: string, description: string, file?: File) => {
    loading.value = true
    error.value = null
    try {
      const updatedPhoto = await apiService.updatePhoto(id, title, description, file)
      const index = photos.value.findIndex(photo => photo.id === id)
      if (index !== -1) {
        photos.value[index] = updatedPhoto
      }
      return updatedPhoto
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update photo'
      console.error('Error updating photo:', err)
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
      const index = photos.value.findIndex(photo => photo.id === id)
      if (index !== -1) {
        photos.value.splice(index, 1)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete photo'
      console.error('Error deleting photo:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getPhotoById = (id: number) => {
    return photos.value.find(photo => photo.id === id)
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // State
    photos,
    loading,
    error,
    
    // Computed
    photoCount,
    hasPhotos,
    
    // Actions
    fetchPhotos,
    addPhoto,
    updatePhoto,
    deletePhoto,
    getPhotoById,
    clearError
  }
})
