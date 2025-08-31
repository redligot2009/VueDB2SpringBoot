<template>
    <div class="gallery-detail-container">
        <div class="gallery-detail">
            <!-- Gallery Header -->
            <div class="gallery-header">
                <div class="gallery-info">
                    <h1>{{ gallery?.name || 'Loading...' }}</h1>
                    <p v-if="gallery?.description" class="gallery-description">{{ gallery.description }}</p>
                    <div class="gallery-stats">
                        <span>{{ totalElements }} photos</span>
                        <span v-if="gallery">• Created {{ formatDate(gallery.createdAt) }}</span>
                    </div>
                </div>
            </div>

            <!-- Gallery Actions -->
            <div class="gallery-actions">
                <!-- Selection mode toggle -->
                <button @click="toggleSelectionMode" class="selection-mode-btn"
                    :class="{ 'active': isSelectionMode }">
                    <font-awesome-icon icon="fa-solid fa-check-square" />
                    {{ isSelectionMode ? 'Cancel Selection' : 'Select Photos' }}
                </button>

                <!-- Bulk delete button (only shown when photos are selected) -->
                <button v-if="isSelectionMode && hasSelection" @click="deleteSelectedPhotos"
                    class="bulk-delete-btn">
                    <font-awesome-icon icon="fa-solid fa-trash" />
                    Delete {{ selectedCount }} Photo{{ selectedCount === 1 ? '' : 's' }}
                </button>

                <!-- Move to gallery button (only shown when photos are selected) -->
                <button v-if="isSelectionMode && hasSelection" @click="showMovePhotosModal" class="move-photos-btn">
                    <font-awesome-icon icon="fa-solid fa-folder-open" />
                    Move {{ selectedCount }} Photo{{ selectedCount === 1 ? '' : 's' }}
                </button>

                <!-- Select all button (only shown in selection mode) -->
                <button v-if="isSelectionMode && photos.length > 0" @click="selectAll" class="select-all-btn">
                    <font-awesome-icon icon="fa-solid fa-check-double" />
                    {{ isAllSelected ? 'Deselect All' : 'Select All' }}
                </button>

                <button @click="showEditGalleryModal" class="btn btn-secondary">
                    <i class="fas fa-edit"></i> Edit Gallery
                </button>
                <button @click="showUploadModal" class="btn btn-primary">
                    <i class="fas fa-upload"></i> Upload Photos
                </button>
                <button @click="showDeleteGalleryModal" class="btn btn-danger">
                    <i class="fas fa-trash"></i> Delete Gallery
                </button>
            </div>

            <!-- Photo Grid -->
            <div class="photo-grid-container">
                <div v-if="loading" class="loading">
                    <i class="fas fa-spinner fa-spin"></i> Loading photos...
                </div>

                <div v-else-if="photos.length === 0" class="empty-state">
                    <i class="fas fa-images"></i>
                    <h3>No photos in this gallery</h3>
                    <p>Upload some photos to get started!</p>
                    <button @click="showUploadModal" class="btn btn-primary">
                        <i class="fas fa-upload"></i> Upload Photos
                    </button>
                </div>

                <div v-else class="photo-grid">
                    <PhotoCard v-for="photo in photos" :key="photo.id" :photo="photo" :show-selection="isSelectionMode"
                        :is-selected="selectedPhotos.includes(photo.id)" @toggle-selection="togglePhotoSelection"
                        @view="viewPhoto" @edit="editPhoto" @delete="deletePhoto" />
                </div>
            </div>

        </div>

        <!-- Sticky Pagination Controls -->
        <StickyPagination :current-page="currentPage" :total-pages="totalPages" :total-elements="totalElements"
            :page-size="pageSize" :loading="loading" @page-change="handlePageChange"
            @page-size-change="handlePageSizeChange" />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePhotoStore } from '@/stores/photoStore'
import { useAuthStore } from '@/stores/authStore'
import { useModalStore } from '@/stores/modalStore'
import { apiService, type Photo } from '@/services/api'
import PhotoCard from '@/components/PhotoCard.vue'
import StickyPagination from '@/components/StickyPagination.vue'


const route = useRoute()
const router = useRouter()
const photoStore = usePhotoStore()
const authStore = useAuthStore()
const modalStore = useModalStore()

// Gallery data
const gallery = ref<any>(null)
const loading = ref(false)
const error = ref<string | null>(null)

// Photo management
const photos = ref<Photo[]>([])
const selectedPhotos = ref<number[]>([])
const isSelectionMode = ref(false)
const currentPage = ref(0)
const pageSize = ref(12)
const totalElements = ref(0)
const totalPages = ref(0)

// Available galleries for moving photos
const availableGalleries = ref<any[]>([])

// Loading states

// Computed properties
const galleryId = computed(() => Number(route.params.id))

// Selection computed properties
const selectedCount = computed(() => selectedPhotos.value.length)
const hasSelection = computed(() => selectedPhotos.value.length > 0)
const isAllSelected = computed(() => {
    return photos.value.length > 0 && selectedPhotos.value.length === photos.value.length
})



// Methods
const loadGallery = async () => {
    try {
        loading.value = true
        error.value = null
        const response = await apiService.getGallery(galleryId.value)
        gallery.value = response

    } catch (err: any) {
        error.value = err.response?.data?.message || 'Failed to load gallery'
        console.error('Error loading gallery:', err)
    } finally {
        loading.value = false
    }
}

const loadPhotos = async () => {
    try {
        const response = await apiService.getAllPhotos(currentPage.value, pageSize.value, galleryId.value)
        photos.value = response.content
        totalElements.value = response.totalElements
        totalPages.value = response.totalPages
    } catch (err: any) {
        console.error('Error loading photos:', err)
    }
}

const loadAvailableGalleries = async () => {
    try {
        const galleries = await apiService.getGalleries()
        // Filter out current gallery
        availableGalleries.value = galleries.filter((g: any) => g.id !== galleryId.value)
    } catch (err: any) {
        console.error('Error loading galleries:', err)
    }
}

const togglePhotoSelection = (photoId: number) => {
    const index = selectedPhotos.value.indexOf(photoId)
    if (index > -1) {
        selectedPhotos.value.splice(index, 1)
    } else {
        selectedPhotos.value.push(photoId)
    }
}

const clearSelection = () => {
    selectedPhotos.value = []
}

const selectAllPhotos = () => {
    selectedPhotos.value = photos.value.map(photo => photo.id)
}

// Selection mode methods
const toggleSelectionMode = () => {
    isSelectionMode.value = !isSelectionMode.value
    if (!isSelectionMode.value) {
        selectedPhotos.value = []
    }
}

const selectAll = () => {
    if (isAllSelected.value) {
        selectedPhotos.value = []
    } else {
        selectedPhotos.value = photos.value.map(photo => photo.id)
    }
}



const handlePageChange = (page: number) => {
    currentPage.value = page
    loadPhotos()
}

const handlePageSizeChange = (size: number) => {
    pageSize.value = size
    currentPage.value = 0 // Reset to first page when changing page size
    loadPhotos()
}

const viewPhoto = (photo: Photo) => {
    // Navigate to photo view with return path to current gallery
    router.push(`/photo/${photo.id}?return=${encodeURIComponent(route.fullPath)}`)
}

const editPhoto = (photo: Photo) => {
    // Navigate to edit photo with return path to current gallery
    router.push(`/edit/${photo.id}?return=${encodeURIComponent(route.fullPath)}`)
}

const deletePhoto = async (photo: Photo) => {
    try {
        await photoStore.deletePhoto(photo.id)
        await loadPhotos()
        clearSelection()
    } catch (err: any) {
        console.error('Error deleting photo:', err)
    }
}

const deleteSelectedPhotos = async () => {
    try {
        for (const photoId of selectedPhotos.value) {
            await photoStore.deletePhoto(photoId)
        }
        await loadPhotos()
        clearSelection()
    } catch (err: any) {
        console.error('Error deleting selected photos:', err)
    }
}

const showEditGalleryModal = () => {
    if (!gallery.value) return

    modalStore.showEditGalleryModal(
        galleryId.value,
        gallery.value.name,
        gallery.value.description || '',
        async (galleryId: number, name: string, description: string) => {
            await apiService.updateGallery(galleryId, { name, description })
            await loadGallery()
        },
        () => {
            // Optional cancel callback
        }
    )
}

const showMovePhotosModal = () => {
    modalStore.showMovePhotosModal(
        selectedPhotos.value,
        availableGalleries.value,
        async (photoIds: number[], targetGalleryId: number | null) => {
            try {
                await apiService.movePhotos({
                    photoIds,
                    targetGalleryId
                })
                await loadPhotos()
                clearSelection()
            } catch (error) {
                console.error('Error moving photos:', error)
                // Re-throw the error so the modal can handle it
                throw error
            }
        },
        () => {
            // Optional cancel callback
        }
    )
}

const showDeleteGalleryModal = () => {
    if (!gallery.value) return

    modalStore.showDeleteGalleryModal(
        galleryId.value,
        gallery.value.name,
        async (galleryId: number, deleteOption: string) => {
            await apiService.deleteGallery(galleryId)
            router.push('/galleries')
        },
        () => {
            // Optional cancel callback
        }
    )
}

const showUploadModal = () => {
    modalStore.showBulkUploadModal(
        async (files: File[], titles?: string[], descriptions?: string[]) => {
            await photoStore.bulkAddPhotos(files, titles, descriptions, galleryId.value)
            // Reload photos after upload
            await loadPhotos()
        },
        () => {
            // Optional cancel callback
        },
        galleryId.value
    )
}



const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString()
}

// Lifecycle
onMounted(async () => {
    if (!authStore.isAuthenticated) {
        router.push('/login')
        return
    }

    await Promise.all([
        loadGallery(),
        loadPhotos(),
        loadAvailableGalleries()
    ])
})

// Watch for route changes
watch(() => route.params.id, async () => {
    if (route.params.id) {
        currentPage.value = 0
        selectedPhotos.value = []
        await Promise.all([
            loadGallery(),
            loadPhotos(),
            loadAvailableGalleries()
        ])
    }
})
</script>

<style scoped>
.gallery-detail-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 2rem;
}

.gallery-detail {
    max-width: 1200px;
    margin: 0 auto;
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.gallery-header {
    padding: 2rem;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-bottom: 1px solid #e1e5e9;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
}

.gallery-info {
    text-align: center;
}

.gallery-info h1 {
    margin: 0 0 0.5rem 0;
    color: #2c3e50;
    font-size: 2.5rem;
    font-weight: 700;
}

.gallery-description {
    color: #6c757d;
    font-size: 1.1rem;
    margin: 0 0 1rem 0;
    line-height: 1.5;
}

.gallery-stats {
    color: #6c757d;
    font-size: 0.9rem;
}

.gallery-stats span {
    margin-right: 1rem;
}

.gallery-actions {
    display: flex;
    padding: 1rem;
    gap: 0.5rem;
    flex-wrap: wrap;
    justify-content: center;
}

.btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    font-size: 0.9rem;
}

.btn-primary {
    background: #007bff;
    color: white;
}

.btn-primary:hover {
    background: #0056b3;
}

.btn-secondary {
    background: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background: #545b62;
}

.btn-danger {
    background: #dc3545;
    color: white;
}

.btn-danger:hover {
    background: #c82333;
}

.btn-outline {
    background: transparent;
    color: #6c757d;
    border: 1px solid #6c757d;
}

.btn-outline:hover {
    background: #6c757d;
    color: white;
}

.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Selection mode buttons */
.selection-mode-btn {
    background: #6c757d;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.selection-mode-btn:hover {
    background: #5a6268;
    transform: translateY(-1px);
}

.selection-mode-btn.active {
    background: #dc3545;
}

.selection-mode-btn.active:hover {
    background: #c82333;
}

.bulk-delete-btn {
    background: #dc3545;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.bulk-delete-btn:hover {
    background: #c82333;
    transform: translateY(-1px);
}

.move-photos-btn {
    background: #17a2b8;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.move-photos-btn:hover {
    background: #138496;
    transform: translateY(-1px);
}

.select-all-btn {
    background: #28a745;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.select-all-btn:hover {
    background: #218838;
    transform: translateY(-1px);
}

.photo-grid-container {
    padding: 2rem;
}

.loading,
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: #6c757d;
}

.loading i {
    font-size: 2rem;
    margin-bottom: 1rem;
}

.empty-state i {
    font-size: 4rem;
    margin-bottom: 1rem;
    color: #dee2e6;
}

.empty-state h3 {
    margin: 0 0 0.5rem 0;
    color: #495057;
}

.empty-state p {
    margin: 0 0 2rem 0;
    color: #6c757d;
}

.photo-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}





.bulk-actions {
    position: sticky;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    background: rgba(255, 255, 255, 0.95);
    border-top: 1px solid rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    z-index: 1000;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    margin-top: auto;
    /* Force hardware acceleration */
    transform: translateZ(0);
    will-change: transform;
}

.bulk-actions-section {
    padding: 1.5rem 2rem;
    background: rgba(248, 250, 252, 0.8);
    border-radius: 0;
    width: 100%;
    max-width: 100%;
}

.bulk-info {
    font-weight: 600;
    color: #4a5568;
    font-size: 0.9rem;
    font-weight: 500;
}

.bulk-buttons {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    padding: 1rem;
}

.modal {
    background: white;
    border-radius: 12px;
    max-width: 500px;
    width: 100%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
    padding: 1.5rem;
    border-bottom: 1px solid #e1e5e9;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
    color: #2c3e50;
}

.modal-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #6c757d;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s ease;
}

.modal-close:hover {
    background: #f8f9fa;
}

.modal-body {
    padding: 1.5rem;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #495057;
}

.form-control {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ced4da;
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.2s ease;
}

.form-control:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    margin-top: 2rem;
}

.upload-area {
    border: 2px dashed #ced4da;
    border-radius: 8px;
    padding: 3rem 2rem;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s ease;
    margin-bottom: 1.5rem;
}

.upload-area:hover {
    border-color: #007bff;
    background: #f8f9fa;
}

.upload-content i {
    font-size: 3rem;
    color: #6c757d;
    margin-bottom: 1rem;
}

.upload-content h4 {
    margin: 0 0 0.5rem 0;
    color: #495057;
}

.upload-content p {
    margin: 0;
    color: #6c757d;
}

.upload-progress {
    margin-top: 1.5rem;
}

.upload-progress h4 {
    margin: 0 0 1rem 0;
    color: #495057;
}

.upload-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 0.5rem;
    padding: 0.5rem;
    background: #f8f9fa;
    border-radius: 4px;
}

.upload-item span {
    flex: 1;
    font-size: 0.9rem;
    color: #495057;
}

.progress-bar {
    width: 100px;
    height: 8px;
    background: #e9ecef;
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: #007bff;
    transition: width 0.3s ease;
}



/* Responsive Design */
@media (max-width: 768px) {
    .gallery-detail-container {
        padding: 1rem;
    }

    .gallery-header {
        flex-direction: column;
        gap: 1rem;
        padding: 1.5rem;
    }

    .gallery-info h1 {
        font-size: 2rem;
    }

    .gallery-actions {
        justify-content: center;
    }

    .photo-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 1rem;
    }

    .bulk-actions-section {
        padding: 1rem;
    }

    .bulk-actions {
        flex-direction: column;
        align-items: stretch;
    }

    .bulk-buttons {
        justify-content: center;
        flex-wrap: wrap;
    }

    .modal {
        margin: 1rem;
        max-width: none;
    }
}
</style>
