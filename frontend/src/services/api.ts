import axios from 'axios'
import { useAuthStore, type User } from '@/stores/authStore'

// Use environment variable for API URL, fallback to localhost for development
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export interface Photo {
  id: number
  title: string
  description?: string
  originalFilename?: string
  contentType?: string
  size?: number
  galleryId?: number
  // data field removed - will be fetched separately when needed
}

export interface PhotoMetadata {
  id: number
  title: string
  description?: string
  originalFilename?: string
  contentType?: string
  size?: number
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
}

export interface UserProfile {
  id: number
  username: string
  email: string
  hasProfilePicture?: boolean
  profilePictureFilename?: string
  profilePictureContentType?: string
  profilePictureSize?: number
}

class ApiService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000, // 60 seconds timeout for file uploads
  })

  // Create a completely clean axios instance for file uploads
  private uploadApi = axios.create({
    baseURL: API_BASE_URL,
    timeout: 120000, // 2 minutes timeout for file uploads
    transformRequest: [(data) => data] // Don't transform FormData
  })

  // Add authentication headers to requests
  private getAuthHeaders() {
    const authStore = useAuthStore()
    return authStore.getAuthHeaders()
  }

  // Add request interceptor for better error handling
  constructor() {
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        // Handle specific error cases
        if (error.response?.status === 413) {
          throw new Error('File size exceeds the maximum limit. Please choose a smaller file.')
        } else if (error.response?.status === 400) {
          const message = error.response.data?.message || error.response.data
          if (typeof message === 'string' && message.includes('File size exceeds')) {
            throw new Error(message)
          } else if (typeof message === 'string' && message.includes('Only image files')) {
            throw new Error('Please select a valid image file (JPG, PNG, GIF, WebP)')
          }
        } else if (error.code === 'ECONNABORTED') {
          throw new Error('Upload timed out. Please try again with a smaller file.')
        } else if (error.code === 'NETWORK_ERROR') {
          throw new Error('Network error. Please check your connection and try again.')
        }
        
        // Re-throw the original error if not handled
        throw error
      }
    )
  }

  // Get all photos with pagination (metadata only)
  async getAllPhotos(page: number = 0, size: number = 10, galleryId?: number): Promise<PaginatedResponse<Photo>> {
    console.log('🌐 Making API call to /photos with auth headers:', this.getAuthHeaders())
    const params: any = { page, size }
    if (galleryId !== undefined) {
      params.galleryId = galleryId
    }
    const response = await this.api.get('/photos', {
      params,
      headers: this.getAuthHeaders()
    })
    console.log('📡 API response received:', response.data)
    return response.data
  }

  // Get photo by ID (metadata only)
  async getPhotoById(id: number): Promise<Photo> {
    const response = await this.api.get(`/photos/${id}`, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  // Get photo metadata only
  async getPhotoMetadata(id: number): Promise<PhotoMetadata> {
    const response = await this.api.get(`/photos/${id}/metadata`, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  // Get photo image data
  async getPhotoImage(id: number): Promise<Blob> {
    const response = await this.api.get(`/photos/${id}/file`, {
      responseType: 'blob',
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  // Create new photo
  async createPhoto(
    title: string,
    description: string,
    file: File,
    galleryId?: number
  ): Promise<Photo> {
    const formData = new FormData()
    formData.append('title', title)
    formData.append('description', description)
    formData.append('file', file)

    // Build URL parameters for gallery ID
    const params = new URLSearchParams()
    if (galleryId !== undefined) {
      params.append('galleryId', galleryId.toString())
    }

    // Use axios with proper FormData handling and URL parameters
    const url = params.toString() ? `/photos?${params.toString()}` : '/photos'
    const response = await this.uploadApi.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...this.getAuthHeaders()
      },
      transformRequest: (data) => data // Prevent axios from transforming FormData
    })
    return response.data
  }

  // Bulk create multiple photos
  async bulkCreatePhotos(
    files: File[],
    titles?: string[],
    descriptions?: string[],
    galleryId?: number
  ): Promise<Photo[]> {
    // Validate inputs
    if (!files || files.length === 0) {
      throw new Error('No files provided for bulk upload')
    }

    // Create FormData with only files
    const formData = new FormData()
    
    // Add files - ensure each file is properly appended
    files.forEach((file, index) => {
      console.log(`Appending file ${index}:`, file.name, file.type, file.size)
      formData.append('files', file, file.name)
    })
    
    // Build URL parameters for titles, descriptions, and gallery ID
    const params = new URLSearchParams()
    
    // Add gallery ID if provided
    if (galleryId !== undefined) {
      params.append('galleryId', galleryId.toString())
    }
    
    // Add titles if provided
    if (titles && titles.length > 0) {
      titles.forEach((title) => {
        if (title && title.trim()) {
          params.append('titles', title.trim())
        }
      })
    }
    
    // Add descriptions if provided
    if (descriptions && descriptions.length > 0) {
      descriptions.forEach((description) => {
        if (description && description.trim()) {
          params.append('descriptions', description.trim())
        }
      })
    }
    
    // Debug: Check FormData contents
    console.log('FormData entries:')
    for (const [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}:`, value.name, value.type, value.size)
      } else {
        console.log(`${key}:`, value)
      }
    }
    
    // Use axios with proper FormData handling and URL parameters
    const url = params.toString() ? `/photos/bulk?${params.toString()}` : '/photos/bulk'
    
    return this.uploadApi.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...this.getAuthHeaders()
      },
      transformRequest: (data) => data // Prevent axios from transforming FormData
    })
  }

  // Update photo
  async updatePhoto(
    id: number,
    title: string,
    description: string,
    file?: File,
    galleryId?: number
  ): Promise<Photo> {
    const formData = new FormData()
    formData.append('title', title)
    formData.append('description', description)
    if (file) {
      formData.append('file', file)
    }

    // Build URL parameters for gallery ID
    const params = new URLSearchParams()
    if (galleryId !== undefined) {
      params.append('galleryId', galleryId.toString())
    }

    // Use axios with proper FormData handling and URL parameters
    const url = params.toString() ? `/photos/${id}?${params.toString()}` : `/photos/${id}`
    const response = await this.uploadApi.put(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...this.getAuthHeaders()
      },
      transformRequest: (data) => data // Prevent axios from transforming FormData
    })
    return response.data
  }

  // Delete photo
  async deletePhoto(id: number): Promise<void> {
    await this.api.delete(`/photos/${id}`, {
      headers: this.getAuthHeaders()
    })
  }

  // Bulk delete photos
  async bulkDeletePhotos(ids: number[]): Promise<void> {
    await this.api.delete('/photos/bulk', {
      headers: this.getAuthHeaders(),
      data: ids
    })
  }

  // Authentication methods
  async login(usernameOrEmail: string, password: string): Promise<{ accessToken: string }> {
    const response = await this.api.post('/auth/signin', {
      usernameOrEmail,
      password
    })
    return response.data
  }

  async signup(username: string, email: string, password: string): Promise<string> {
    const response = await this.api.post('/auth/signup', {
      username,
      email,
      password
    })
    return response.data
  }

  // Get current user information
  async getCurrentUser(): Promise<User> {
    const response = await this.api.get('/auth/me', {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  // Update user profile
  async updateProfile(username: string, email: string, password?: string): Promise<string> {
    // Create empty FormData (no profile picture parameter at all)
    const formData = new FormData()
    
    // Build URL parameters for form fields
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('email', email)
    
    // Only include password if provided
    if (password && password.trim()) {
      params.append('password', password)
    }
    
    // Note: No profilePicture parameter is added to FormData
    // This indicates "no profile picture change"
    
    // Use axios with proper FormData handling and URL parameters
    const url = `/auth/profile?${params.toString()}`
    
    const response = await this.api.put(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...this.getAuthHeaders()
      },
      transformRequest: (data) => data // Prevent axios from transforming FormData
    })
    return response.data
  }

  // Get user profile picture
  async getProfilePicture(): Promise<Blob> {
    const response = await this.api.get('/auth/profile-picture', {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    })
    return response.data
  }



  // Update user profile with picture
  async updateProfileWithPicture(username: string, email: string, password?: string, profilePicture?: File | null): Promise<string> {
    // Create FormData with only the file
    const formData = new FormData()
    
    // Always include profilePicture parameter to indicate intent
    if (profilePicture) {
      console.log('Appending profile picture:', profilePicture.name, profilePicture.type, profilePicture.size)
      formData.append('profilePicture', profilePicture, profilePicture.name)
    } else {
      // Create an empty file with special filename to indicate profile picture removal
      console.log('Appending removal indicator for profile picture')
      const emptyFile = new File([''], 'REMOVE_PROFILE_PICTURE', { type: 'text/plain' })
      formData.append('profilePicture', emptyFile, 'REMOVE_PROFILE_PICTURE')
    }
    
    // Build URL parameters for form fields
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('email', email)
    
    // Only include password if provided
    if (password && password.trim()) {
      params.append('password', password)
    }
    
    // Debug: Check FormData contents
    console.log('🔍 Profile update FormData contents:')
    for (const [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}:`, value.name, value.type, value.size)
      } else {
        console.log(`${key}:`, value)
      }
    }
    console.log('🔍 URL params:', params.toString())
    
    // Use axios with proper FormData handling and URL parameters
    const url = `/auth/profile?${params.toString()}`
    
    const response = await this.api.put(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...this.getAuthHeaders()
      },
      transformRequest: (data) => data // Prevent axios from transforming FormData
    })
    return response.data
  }

  // Gallery API methods
  async getGalleries(): Promise<any[]> {
    const response = await this.api.get('/galleries', {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  async createGallery(galleryData: { name: string; description?: string }): Promise<any> {
    const response = await this.api.post('/galleries', galleryData, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  async getGallery(galleryId: number): Promise<any> {
    const response = await this.api.get(`/galleries/${galleryId}`, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  async updateGallery(galleryId: number, galleryData: { name: string; description?: string }): Promise<any> {
    const response = await this.api.put(`/galleries/${galleryId}`, galleryData, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  async deleteGallery(galleryId: number): Promise<string> {
    const response = await this.api.delete(`/galleries/${galleryId}`, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }

  async movePhotos(moveData: { photoIds: number[]; targetGalleryId: number | null }): Promise<string> {
    const response = await this.api.post('/galleries/move-photos', moveData, {
      headers: this.getAuthHeaders()
    })
    return response.data
  }
}

export const apiService = new ApiService()
