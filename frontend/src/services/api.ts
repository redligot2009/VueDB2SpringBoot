import axios from 'axios'

// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export interface Photo {
  id: number
  title: string
  description?: string
  originalFilename?: string
  contentType?: string
  size?: number
  data?: string // base64 encoded image data
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

class ApiService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000, // 60 seconds timeout for file uploads
  })

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
  async getAllPhotos(page: number = 0, size: number = 10): Promise<PaginatedResponse<Photo>> {
    const response = await this.api.get('/photos', {
      params: { page, size }
    })
    return response.data
  }

  // Get photo by ID (metadata only)
  async getPhotoById(id: number): Promise<Photo> {
    const response = await this.api.get(`/photos/${id}`)
    return response.data
  }

  // Get photo metadata only
  async getPhotoMetadata(id: number): Promise<PhotoMetadata> {
    const response = await this.api.get(`/photos/${id}/metadata`)
    return response.data
  }

  // Get photo image data
  async getPhotoImage(id: number): Promise<Blob> {
    const response = await this.api.get(`/photos/${id}/file`, {
      responseType: 'blob'
    })
    return response.data
  }

  // Create new photo
  async createPhoto(
    title: string,
    description: string,
    file: File
  ): Promise<Photo> {
    const formData = new FormData()
    formData.append('title', title)
    formData.append('description', description)
    formData.append('file', file)

    const response = await this.api.post('/photos', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        // You can add upload progress tracking here if needed
        const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1))
        console.log(`Upload progress: ${percentCompleted}%`)
      }
    })
    return response.data
  }

  // Update photo
  async updatePhoto(
    id: number,
    title: string,
    description: string,
    file?: File
  ): Promise<Photo> {
    const formData = new FormData()
    formData.append('title', title)
    formData.append('description', description)
    if (file) {
      formData.append('file', file)
    }

    const response = await this.api.put(`/photos/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      }
    })
    return response.data
  }

  // Delete photo
  async deletePhoto(id: number): Promise<void> {
    await this.api.delete(`/photos/${id}`)
  }
}

export const apiService = new ApiService()
