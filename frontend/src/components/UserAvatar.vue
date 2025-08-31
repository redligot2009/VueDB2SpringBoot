<template>
  <div class="user-avatar" :class="[`user-avatar-${props.size}`, { 'loading': isLoading }]">
    <div 
      v-if="pictureUrl && !isLoading" 
      :style="{ backgroundImage: `url(${pictureUrl})` }"
      class="avatar-image"
    />
    <div v-else-if="isLoading" class="avatar-loading">
      <div class="loading-spinner"></div>
    </div>
    <div 
      v-else-if="defaultAvatarUrl" 
      :style="{ backgroundImage: `url(${defaultAvatarUrl})` }"
      class="avatar-image"
    />
    <div v-else class="avatar-fallback">
      {{ initials }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useProfilePicture } from '@/composables/useProfilePicture'
import { getDefaultAvatarUrl } from '@/utils/avatar'

interface Props {
  username: string
  hasProfilePicture?: boolean
  size?: 'small' | 'medium' | 'large' | 'extra-large'
}

const props = withDefaults(defineProps<Props>(), {
  hasProfilePicture: false,
  size: 'medium'
})

// Create a unique instance key for this component
const instanceKey = `avatar-${props.username}-${props.size}`

const { pictureUrl, isLoading, loadProfilePicture, clearProfilePicture } = useProfilePicture(instanceKey)

// Extract initials from username
const initials = computed(() => {
  return props.username
    .split(' ')
    .map(word => word.charAt(0).toUpperCase())
    .slice(0, 2)
    .join('')
})

// Default avatar URL
const defaultAvatarUrl = computed(() => {
  try {
    return getDefaultAvatarUrl(props.username)
  } catch (error) {
    return null
  }
})

// Handle image loading error
const handleImageError = () => {
  // Fallback to default avatar - the composable will handle this
  if (pictureUrl.value && pictureUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(pictureUrl.value)
  }
}

// Track if we've already loaded the profile picture
let hasLoadedProfilePicture = false

// Watch for prop changes and reload if necessary
watch(() => [props.username, props.hasProfilePicture], async (newValues, oldValues) => {
  const [newUsername, newHasProfilePicture] = newValues as [string, boolean]
  const [oldUsername, oldHasProfilePicture] = (oldValues || []) as [string, boolean]
  
  console.log('🔄 UserAvatar watch triggered:', {
    newUsername,
    newHasProfilePicture,
    oldUsername,
    oldHasProfilePicture,
    changed: newUsername !== oldUsername || newHasProfilePicture !== oldHasProfilePicture
  })
  
  // Always reload when hasProfilePicture changes, or when username changes
  if (newUsername !== oldUsername || newHasProfilePicture !== oldHasProfilePicture) {
    console.log('🔄 Reloading profile picture...')
    // Clear any existing profile picture first
    clearProfilePicture()
    hasLoadedProfilePicture = false
    
    // Load the new profile picture
    await loadProfilePicture(newUsername, newHasProfilePicture)
    hasLoadedProfilePicture = true
    console.log('✅ Profile picture reloaded')
  }
}, { immediate: true })

// Clean up on unmount
onUnmounted(() => {
  clearProfilePicture()
})
</script>

<style scoped>
.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
  position: relative;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.user-avatar-medium {
  width: 40px;
  height: 40px;
  font-size: 16px;
}

.user-avatar-large {
  width: 64px;
  height: 64px;
  font-size: 24px;
}

.user-avatar-extra-large {
  width: 96px;
  height: 96px;
  font-size: 32px;
}

.avatar-image {
  width: 32px;
  height: 32px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: 50%;
}

.user-avatar-small .avatar-image {
  width: 32px;
  height: 32px;
}

.user-avatar-medium .avatar-image {
  width: 40px;
  height: 40px;
}

.user-avatar-large .avatar-image {
  width: 64px;
  height: 64px;
}

.user-avatar-extra-large .avatar-image {
  width: 96px;
  height: 96px;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #667eea; /* Solid color fallback */
  color: white;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.avatar-loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.user-avatar.loading {
  opacity: 0.7;
}
</style>
