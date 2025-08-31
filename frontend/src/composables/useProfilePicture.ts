import { ref, computed } from 'vue'
import { apiService } from '@/services/api'
import { getDefaultAvatarUrl } from '@/utils/avatar'

// Store for managing multiple profile picture instances
const profilePictureStore = new Map<string, {
  profilePictureUrl: string | null
  isLoading: boolean
  error: string | null
}>()

export function useProfilePicture(instanceKey: string = 'default') {
  // Get or create instance data
  if (!profilePictureStore.has(instanceKey)) {
    profilePictureStore.set(instanceKey, {
      profilePictureUrl: null,
      isLoading: false,
      error: null
    })
  }
  
  const instance = profilePictureStore.get(instanceKey)!
  
  const profilePictureUrl = ref<string | null>(instance.profilePictureUrl)
  const isLoading = ref<boolean>(instance.isLoading)
  const error = ref<string | null>(instance.error)

  /**
   * Load profile picture for a user
   * @param username - Username for fallback avatar
   * @param hasProfilePicture - Whether user has uploaded a profile picture
   */
  const loadProfilePicture = async (username: string, hasProfilePicture: boolean = false) => {
    if (!hasProfilePicture) {
      // Use default avatar
      const defaultUrl = getDefaultAvatarUrl(username)
      profilePictureUrl.value = defaultUrl
      instance.profilePictureUrl = defaultUrl
      return
    }

    try {
      isLoading.value = true
      instance.isLoading = true
      error.value = null
      instance.error = null
      
      const blob = await apiService.getProfilePicture()
      const url = URL.createObjectURL(blob)
      profilePictureUrl.value = url
      instance.profilePictureUrl = url
    } catch (err) {
      console.error('Error loading profile picture:', err)
      error.value = 'Failed to load profile picture'
      instance.error = 'Failed to load profile picture'
      // Fallback to default avatar
      const defaultUrl = getDefaultAvatarUrl(username)
      profilePictureUrl.value = defaultUrl
      instance.profilePictureUrl = defaultUrl
    } finally {
      isLoading.value = false
      instance.isLoading = false
    }
  }

  /**
   * Clear profile picture URL and revoke object URL
   */
  const clearProfilePicture = () => {
    if (profilePictureUrl.value && profilePictureUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(profilePictureUrl.value)
    }
    profilePictureUrl.value = null
    instance.profilePictureUrl = null
    error.value = null
    instance.error = null
  }

  /**
   * Get profile picture URL (computed)
   */
  const pictureUrl = computed(() => profilePictureUrl.value)

  return {
    pictureUrl,
    isLoading,
    error,
    loadProfilePicture,
    clearProfilePicture
  }
}
