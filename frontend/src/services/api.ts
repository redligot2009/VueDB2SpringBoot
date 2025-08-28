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

class ApiService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json'
    }
  })

  // Get all photos (metadata only)
  async getAllPhotos(): Promise<Photo[]> {
    const response = await this.api.get<Photo[]>('/photos')
    return response.data
  }

  // Get photo by ID (metadata only)
  async getPhotoById(id: number): Promise<Photo> {
    const response = await this.api.get<Photo>(`/photos/${id}`)
    return response.data
  }

  // Get photo metadata only
  async getPhotoMetadata(id: number): Promise<PhotoMetadata> {
    const response = await this.api.get<PhotoMetadata>(`/photos/${id}/metadata`)
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
    if (description) {
      formData.append('description', description)
    }
    formData.append('file', file)

    const response = await this.api.post<Photo>('/photos', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
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
    if (description) {
      formData.append('description', description)
    }
    if (file) {
      formData.append('file', file)
    }

    const response = await this.api.put<Photo>(`/photos/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
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
