import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { usePhotoStore } from './photoStore'
import { isTokenExpired, getUserIdFromToken } from '@/utils/jwt'
import { apiService, type UserProfile } from '@/services/api'

export interface User {
  id: number
  username: string
  email: string
}

// Extended user interface for profile data
export interface UserWithProfile extends User {
  hasProfilePicture?: boolean
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const userProfile = ref<UserProfile | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const profilePictureUpdateTime = ref<number>(Date.now())

  // Computed
  const isAuthenticated = computed(() => {
    if (!token.value) return false
    // Check if token is expired
    if (isTokenExpired(token.value)) {
      // Auto-logout if token is expired
      logout()
      return false
    }
    return true
  })

  // Actions
  const login = (userData: User, authToken: string) => {
    // Validate token before storing
    if (isTokenExpired(authToken)) {
      throw new Error('Token is expired')
    }
    
    const userId = getUserIdFromToken(authToken)
    if (!userId || userId !== userData.id) {
      throw new Error('Token user ID does not match provided user data')
    }
    
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    // Clear any other cached data if needed
    sessionStorage.clear()
    
    // Clear photo store data when logging out
    try {
      const photoStore = usePhotoStore()
      photoStore.clearStore()
    } catch (error) {
      console.warn('Could not clear photo store:', error)
    }
  }

  const setUser = (userData: User) => {
    user.value = userData
  }

  const getAuthHeaders = () => {
    return token.value ? { Authorization: `Bearer ${token.value}` } : {}
  }

  const getCurrentUserId = (): number | null => {
    return token.value ? getUserIdFromToken(token.value) : null
  }

  const refreshUserData = async () => {
    try {
      if (!token.value) {
        throw new Error('No token available')
      }
      
      const userData = await apiService.getCurrentUser()
      // Store full profile data
      userProfile.value = userData
      // Convert UserProfile to User for backward compatibility
      user.value = {
        id: userData.id,
        username: userData.username,
        email: userData.email
      }
      // Update the profile picture update timestamp
      profilePictureUpdateTime.value = Date.now()
    } catch (error) {
      console.error('Error refreshing user data:', error)
      // If we can't refresh user data, logout the user
      logout()
      throw error
    }
  }

  return {
    // State
    user,
    userProfile,
    token,
    profilePictureUpdateTime,
    // Computed
    isAuthenticated,
    // Actions
    login,
    logout,
    setUser,
    getAuthHeaders,
    getCurrentUserId,
    refreshUserData
  }
})
