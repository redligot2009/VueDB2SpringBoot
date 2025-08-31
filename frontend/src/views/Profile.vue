<template>
  <div class="profile-container">
    <div class="profile-card">
      <h1>Edit Profile</h1>

      <form @submit.prevent="handleSubmit" class="profile-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input id="username" v-model="form.username" type="text" required minlength="3" maxlength="50"
            :disabled="loading" class="form-input" />
          <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" v-model="form.email" type="email" required :disabled="loading" class="form-input" />
          <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
        </div>

        <div class="form-group">
          <label for="password">New Password (leave blank to keep current)</label>
          <input id="password" v-model="form.password" type="password" minlength="6" maxlength="100" :disabled="loading"
            class="form-input" />
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm New Password</label>
          <input id="confirmPassword" v-model="form.confirmPassword" type="password" :disabled="loading"
            class="form-input" />
          <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
        </div>

        <div class="form-group">
          <label>Profile Picture</label>
          <div class="profile-picture-section">
            <div class="current-picture">
              <UserAvatar :key="`profile-avatar-${avatarKey}`" :username="authStore.user?.username || ''"
                :has-profile-picture="authStore.userProfile?.hasProfilePicture || false" size="extra-large" />
              <span class="current-picture-label">
                {{ authStore.userProfile?.hasProfilePicture ? 'Current profile picture' : 'Default avatar' }}
              </span>
              <button v-if="authStore.userProfile?.hasProfilePicture" type="button" @click="removeCurrentProfilePicture"
                class="remove-current-picture-btn" :disabled="loading">
                Remove Profile Picture
              </button>
            </div>



            <div class="file-upload-section">
              <div class="file-upload-container" v-if="!profilePicturePreview">
                <input id="profilePicture" type="file" accept="image/*" @change="handleProfilePictureChange"
                  :disabled="loading" class="file-input" />
                <div class="file-upload-placeholder">
                  <span>Click to select a new profile picture</span>
                </div>
              </div>
              <div class="file-upload-preview" v-else>
                <div :style="{ backgroundImage: `url(${profilePicturePreview})` }" class="preview-image" />
                <div class="preview-actions">
                  <button type="button" @click="removeProfilePicture" class="remove-picture-btn">Remove</button>
                </div>
              </div>
            </div>
          </div>
          <span class="help-text">Upload a new profile picture (JPG, PNG, GIF, WebP). Max 5MB.</span>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="loading" class="btn btn-primary">
            {{ loading ? 'Updating...' : 'Update Profile' }}
          </button>
          <button type="button" @click="loadUserData" :disabled="loading" class="btn btn-secondary">
            Reset
          </button>
        </div>
      </form>

      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { apiService } from '@/services/api'
import UserAvatar from '@/components/UserAvatar.vue'

const router = useRouter()
const authStore = useAuthStore()

// Form data
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// Form state
const loading = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')

// Profile picture state
const profilePicture = ref<File | null>(null)
const profilePicturePreview = ref<string | null>(null)

// Avatar key for forcing re-render when profile picture changes
const avatarKey = ref(0)

// Validation errors
const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// Load current user data
const loadUserData = async () => {
  try {
    loading.value = true
    // Use auth store to refresh user data (includes profile picture info)
    await authStore.refreshUserData()

    // Update form with current user data
    form.username = authStore.user?.username || ''
    form.email = authStore.user?.email || ''
    form.password = ''
    form.confirmPassword = ''

    // Clear any previous messages
    message.value = ''
    clearErrors()
  } catch (error: any) {
    console.error('Error loading user data:', error)
    message.value = 'Failed to load user data'
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}

// Clear validation errors
const clearErrors = () => {
  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
}

// Validate form
const validateForm = (): boolean => {
  clearErrors()
  let isValid = true

  // Username validation
  if (!form.username.trim()) {
    errors.username = 'Username is required'
    isValid = false
  } else if (form.username.length < 3) {
    errors.username = 'Username must be at least 3 characters'
    isValid = false
  } else if (form.username.length > 50) {
    errors.username = 'Username must be less than 50 characters'
    isValid = false
  }

  // Email validation
  if (!form.email.trim()) {
    errors.email = 'Email is required'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Please enter a valid email address'
    isValid = false
  }

  // Password validation (only if provided)
  if (form.password.trim()) {
    if (form.password.length < 6) {
      errors.password = 'Password must be at least 6 characters'
      isValid = false
    } else if (form.password.length > 100) {
      errors.password = 'Password must be less than 100 characters'
      isValid = false
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match'
      isValid = false
    }
  }

  return isValid
}

// Profile picture handlers
const handleProfilePictureChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      message.value = 'Please select a valid image file.'
      messageType.value = 'error'
      return
    }

    // Validate file size (5MB limit)
    if (file.size > 5 * 1024 * 1024) {
      message.value = 'Profile picture must be less than 5MB.'
      messageType.value = 'error'
      return
    }

    profilePicture.value = file

    // Create preview
    const reader = new FileReader()
    reader.onload = (e) => {
      profilePicturePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)

    message.value = ''
  }
}

const removeProfilePicture = () => {
  profilePicture.value = null
  profilePicturePreview.value = null

  // Clear the file input
  const fileInput = document.getElementById('profilePicture') as HTMLInputElement
  if (fileInput) {
    fileInput.value = ''
  }

  // Clear any error messages
  message.value = ''
}

const removeCurrentProfilePicture = async () => {
  try {
    loading.value = true
    message.value = ''

    // Use the existing profile update endpoint with null to remove profile picture
    const result = await apiService.updateProfileWithPicture(
      authStore.user?.username || '',
      authStore.user?.email || '',
      undefined, // no password change
      null // null to remove profile picture
    )

    message.value = 'Profile picture removed successfully!'
    messageType.value = 'success'

    // Update auth store with new user data
    await authStore.refreshUserData()

    // Force avatar re-render by updating the key
    avatarKey.value++

  } catch (error: any) {
    console.error('Error removing profile picture:', error)

    if (error.response?.data) {
      message.value = error.response.data
    } else {
      message.value = 'Failed to remove profile picture. Please try again.'
    }
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}



// Handle form submission
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    loading.value = true
    message.value = ''

    // Call API to update profile
    let result: string
    if (profilePicture.value) {
      // Use multipart endpoint for profile update with picture
      result = await apiService.updateProfileWithPicture(
        form.username.trim(),
        form.email.trim(),
        form.password.trim() || undefined,
        profilePicture.value
      )
    } else {
      // Use JSON endpoint for profile update without picture
      result = await apiService.updateProfile(
        form.username.trim(),
        form.email.trim(),
        form.password.trim() || undefined
      )
    }

    message.value = 'Profile updated successfully!'
    messageType.value = 'success'

    // Clear password fields and profile picture
    form.password = ''
    form.confirmPassword = ''
    profilePicture.value = null
    profilePicturePreview.value = null

    // Update auth store with new user data
    await authStore.refreshUserData()

    // Force avatar re-render by updating the key
    avatarKey.value++

  } catch (error: any) {
    console.error('Error updating profile:', error)

    if (error.response?.data) {
      message.value = error.response.data
    } else {
      message.value = 'Failed to update profile. Please try again.'
    }
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}

// Load user data on component mount
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.profile-card h1 {
  margin: 0 0 2rem 0;
  color: #333;
  text-align: center;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #555;
  font-size: 0.9rem;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.form-input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #545b62;
}

.message {
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 4px;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* Profile picture styles */
.profile-picture-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.current-picture {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.current-picture-label {
  font-size: 0.875rem;
  color: #6c757d;
  font-weight: 500;
}

.file-upload-container {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
  transition: border-color 0.2s ease;
  cursor: pointer;
  position: relative;
}

.file-upload-container:hover {
  border-color: #007bff;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.file-upload-placeholder {
  color: #6b7280;
  font-size: 0.875rem;
}

.file-upload-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.file-upload-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background-color: #f9fafb;
}

.preview-actions {
  display: flex;
  gap: 0.5rem;
}

.preview-image {
  width: 160px;
  height: 160px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  flex-shrink: 0;
}

.remove-picture-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.remove-picture-btn:hover {
  background: #dc2626;
}

.remove-current-picture-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-top: 0.5rem;
}

.remove-current-picture-btn:hover:not(:disabled) {
  background: #dc2626;
}

.remove-current-picture-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.help-text {
  color: #6b7280;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

/* Direct profile picture display styles */
.direct-profile-picture {
  margin: 1rem 0;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.direct-profile-picture h4 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1rem;
}

.profile-picture-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.profile-picture-loading {
  padding: 2rem;
  text-align: center;
  color: #6b7280;
  background-color: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
}

.profile-picture-info {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.profile-picture-info p {
  margin: 0.25rem 0;
  font-size: 0.875rem;
  color: #374151;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .profile-container {
    margin: 1rem auto;
  }

  .profile-card {
    padding: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
