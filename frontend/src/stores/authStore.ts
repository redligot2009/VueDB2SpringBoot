import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { usePhotoStore } from './photoStore'
import { isTokenExpired, getUserIdFromToken } from '@/utils/jwt'

export interface User {
  id: number
  username: string
  email: string
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

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



  return {
    // State
    user,
    token,
    // Computed
    isAuthenticated,
    // Actions
    login,
    logout,
    setUser,
    getAuthHeaders,
    getCurrentUserId
  }
})
