<template>
  <div class="photo-view-container">
    <div class="photo-view-header">
      <button @click="goBack" class="back-btn">
        <font-awesome-icon icon="fa-solid fa-arrow-left" /> Back to Gallery
      </button>
      <div class="photo-info">
        <h1>{{ photo?.title || 'Photo' }}</h1>
        <p v-if="photo?.description" class="photo-description">{{ photo.description }}</p>
      </div>
      <div class="zoom-controls">
        <button @click="zoomOut" class="zoom-btn" :disabled="scale <= 0.5">
          <font-awesome-icon icon="fa-solid fa-minus" />
        </button>
        <span class="zoom-level">{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomIn" class="zoom-btn" :disabled="scale >= 3">
          <font-awesome-icon icon="fa-solid fa-plus" />
        </button>
        <button @click="resetZoom" class="reset-btn">
          <font-awesome-icon icon="fa-solid fa-arrows-to-dot" />
        </button>
      </div>
    </div>

    <div class="photo-view-content" ref="containerRef">
      <div class="photo-wrapper" :style="{
        transform: `scale(${scale}) translate(${panOffset.x}px, ${panOffset.y}px)`
      }" @wheel="handleWheel" @mousedown="startPan" @mousemove="pan" @mouseup="stopPan" @mouseleave="stopPan">
        <img v-if="imageUrl" :src="imageUrl" :alt="photo?.title || 'Photo'" class="photo-image" @load="onImageLoad"
          ref="imageRef" draggable="false" />
      </div>
    </div>

    <div class="photo-navigation" v-if="hasMultiplePhotos">
      <button @click="previousPhoto" class="nav-btn prev-btn" :disabled="!hasPreviousPhoto">
        <font-awesome-icon icon="fa-solid fa-chevron-left" />
      </button>
      <span class="photo-counter">{{ currentPhotoIndex + 1 }} of {{ totalPhotos }}</span>
      <button @click="nextPhoto" class="nav-btn next-btn" :disabled="!hasNextPhoto">
        <font-awesome-icon icon="fa-solid fa-chevron-right" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePhotoStore } from '@/stores/photoStore'
import type { Photo } from '@/services/api'
import { apiService, API_BASE_URL } from '@/services/api'

const route = useRoute()
const router = useRouter()
const photoStore = usePhotoStore()

// Photo data
const photo = ref<Photo | null>(null)
const currentPhotoIndex = ref(0)
const totalPhotos = ref(0)
const imageUrl = ref<string | null>(null)

// Zoom and pan state
const scale = ref(1)
const isPanning = ref(false)
const panStart = ref({ x: 0, y: 0 })
const panOffset = ref({ x: 0, y: 0 })

// Refs
const containerRef = ref<HTMLElement | null>(null)
const imageRef = ref<HTMLImageElement | null>(null)

// Computed properties
const hasMultiplePhotos = computed(() => totalPhotos.value > 1)
const hasPreviousPhoto = computed(() => currentPhotoIndex.value > 0)
const hasNextPhoto = computed(() => currentPhotoIndex.value < totalPhotos.value - 1)

// Load photo data
const loadPhoto = async () => {
  const photoId = parseInt(route.params.id as string)
  if (isNaN(photoId)) {
    router.push('/gallery')
    return
  }

  try {
    // Find photo in store or fetch if needed
    const foundPhoto = photoStore.photos.find(p => p.id === photoId)
    if (foundPhoto) {
      photo.value = foundPhoto
      currentPhotoIndex.value = photoStore.photos.findIndex(p => p.id === photoId)
      totalPhotos.value = photoStore.photoCount

      // Load the image data
      await loadImage(photoId)
    } else {
      // If photo not in store, redirect to gallery
      router.push('/gallery')
    }
  } catch (error) {
    console.error('Failed to load photo:', error)
    router.push('/gallery')
  }
}

// Load image data
const loadImage = async (photoId: number) => {
  try {
    const blob = await apiService.getPhotoImage(photoId)
    imageUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('Error loading image:', error)
    // Handle error - could show a placeholder or error message
  }
}

// Navigation
const goBack = () => {
  router.push('/gallery')
}

const previousPhoto = async () => {
  if (hasPreviousPhoto.value) {
    const prevPhoto = photoStore.photos[currentPhotoIndex.value - 1]
    router.push(`/photo/${prevPhoto.id}`)
  }
}

const nextPhoto = async () => {
  if (hasNextPhoto.value) {
    const nextPhoto = photoStore.photos[currentPhotoIndex.value + 1]
    router.push(`/photo/${nextPhoto.id}`)
  }
}

// Zoom controls
const zoomIn = () => {
  if (scale.value < 3) {
    scale.value = Math.min(3, scale.value + 0.25)
  }
}

const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value = Math.max(0.5, scale.value - 0.25)
  }
}

const resetZoom = () => {
  scale.value = 1
  panOffset.value = { x: 0, y: 0 }
}

// Mouse wheel zoom
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()

  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newScale = Math.max(0.5, Math.min(3, scale.value + delta))

  if (newScale !== scale.value) {
    scale.value = newScale
  }
}

// Pan functionality
const startPan = (event: MouseEvent) => {
  if (scale.value > 1) {
    event.preventDefault()
    isPanning.value = true
    panStart.value = { x: event.clientX, y: event.clientY }
  }
}

const pan = (event: MouseEvent) => {
  if (isPanning.value && scale.value > 1) {
    event.preventDefault()
    const deltaX = event.clientX - panStart.value.x
    const deltaY = event.clientY - panStart.value.y

    // Calculate new pan offset
    const newPanX = panOffset.value.x + deltaX
    const newPanY = panOffset.value.y + deltaY

    // Get container dimensions
    const container = containerRef.value
    const image = imageRef.value

    if (container && image) {
      const containerRect = container.getBoundingClientRect()
      const imageRect = image.getBoundingClientRect()

      // Get the natural image dimensions
      const naturalWidth = image.naturalWidth
      const naturalHeight = image.naturalHeight

      // Calculate the displayed image size (after object-fit: contain)
      const containerAspect = containerRect.width / containerRect.height
      const imageAspect = naturalWidth / naturalHeight

      let displayedWidth, displayedHeight
      if (imageAspect > containerAspect) {
        // Image is wider than container
        displayedWidth = containerRect.width
        displayedHeight = containerRect.width / imageAspect
      } else {
        // Image is taller than container
        displayedHeight = containerRect.height
        displayedWidth = containerRect.height * imageAspect
      }

      // Calculate scaled dimensions
      const scaledWidth = displayedWidth * scale.value
      const scaledHeight = displayedHeight * scale.value

      // Calculate maximum allowed pan distance
      // At high zoom levels, we need much tighter bounds
      const maxPanX = Math.max(0, (scaledWidth - containerRect.width) / 2)
      const maxPanY = Math.max(0, (scaledHeight - containerRect.height) / 2)

      // Apply much tighter bounds for high zoom levels
      // When zoomed in significantly, we want to ensure the image stays within view
      const tightBoundX = Math.min(maxPanX, containerRect.width * 0.5) // 10% of container width max
      const tightBoundY = Math.min(maxPanY, containerRect.height * 0.5) // 10% of container height max

      panOffset.value = {
        x: Math.max(-tightBoundX, Math.min(tightBoundX, newPanX)),
        y: Math.max(-tightBoundY, Math.min(tightBoundY, newPanY))
      }
    } else {
      // Fallback if refs aren't available
      panOffset.value = {
        x: newPanX,
        y: newPanY
      }
    }

    panStart.value = { x: event.clientX, y: event.clientY }
  }
}

const stopPan = () => {
  isPanning.value = false
}

// Image load handler
const onImageLoad = () => {
  resetZoom()
}

// Watch for route changes
const watchRoute = () => {
  loadPhoto()
}

onMounted(() => {
  loadPhoto()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  // Clean up the image URL to prevent memory leaks
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
  }
})

// Watch for route changes to reload photo when navigating
watch(() => route.params.id, async (newId) => {
  if (newId) {
    // Clean up previous image URL
    if (imageUrl.value) {
      URL.revokeObjectURL(imageUrl.value)
      imageUrl.value = null
    }
    // Reset zoom
    resetZoom()
    // Load new photo
    await loadPhoto()
  }
})

// Keyboard navigation
const handleKeydown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Escape':
      goBack()
      break
    case 'ArrowLeft':
      if (hasPreviousPhoto.value) {
        previousPhoto()
      }
      break
    case 'ArrowRight':
      if (hasNextPhoto.value) {
        nextPhoto()
      }
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
      zoomOut()
      break
    case '0':
      resetZoom()
      break
  }
}
</script>

<style scoped>
.photo-view-container {
  width: 100%;
  height: 80vh;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  background: #000;
  color: white;
  overflow: hidden;
}

.photo-view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.photo-info {
  text-align: center;
  flex: 1;
  margin: 0 2rem;
}

.photo-info h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: white;
}

.photo-description {
  margin: 0.5rem 0 0 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.zoom-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.zoom-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.zoom-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.zoom-level {
  font-weight: 600;
  min-width: 60px;
  text-align: center;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.photo-view-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  cursor: grab;
  min-height: 0;
  max-height: calc(80vh - 140px);
  /* Account for header and navigation */
}

.photo-view-content:active {
  cursor: grabbing;
}

.photo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.1s ease;
  transform-origin: center center;
  max-width: 100%;
  max-height: 100%;
}

.photo-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  pointer-events: auto;
  -webkit-user-drag: none;
  -khtml-user-drag: none;
  -moz-user-drag: none;
  -o-user-drag: none;
  user-drag: none;
}

.photo-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  padding: 1rem 2rem;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.photo-counter {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  .photo-view-header {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .photo-info {
    margin: 0;
    order: 2;
  }

  .back-btn {
    order: 1;
    align-self: flex-start;
  }

  .zoom-controls {
    order: 3;
    align-self: center;
  }

  .photo-info h1 {
    font-size: 1.2rem;
  }

  .photo-navigation {
    gap: 1rem;
    padding: 1rem;
  }

  .nav-btn {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }
}
</style>
