<template>
  <div class="signup-container">
    <div class="signup-card">
      <div class="signup-header">
        <h1>Create Account</h1>
        <p>Join us to start uploading and managing your photos</p>
      </div>

      <form @submit.prevent="handleSignup" class="signup-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            required
            minlength="3"
            maxlength="50"
            placeholder="Choose a username (3-50 characters)"
            :disabled="isLoading"
          />
          <span class="help-text">Must be between 3 and 50 characters</span>
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            placeholder="Enter your email address"
            :disabled="isLoading"
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            minlength="6"
            maxlength="40"
            placeholder="Create a password (6-40 characters)"
            :disabled="isLoading"
          />
          <span class="help-text">Must be between 6 and 40 characters</span>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            required
            placeholder="Confirm your password"
            :disabled="isLoading"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div v-if="success" class="success-message">
          {{ success }}
        </div>

        <button type="submit" class="signup-button" :disabled="isLoading || !isFormValid">
          <span v-if="isLoading" class="loading-spinner"></span>
          {{ isLoading ? 'Creating Account...' : 'Create Account' }}
        </button>
      </form>

      <div class="signup-footer">
        <p>
          Already have an account?
          <router-link to="/login" class="link">Sign in here</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiService } from '@/services/api'

const router = useRouter()

const isLoading = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  return (
    form.username.length >= 3 &&
    form.username.length <= 50 &&
    form.email.includes('@') &&
    form.password.length >= 6 &&
    form.password.length <= 40 &&
    form.password === form.confirmPassword
  )
})

const handleSignup = async () => {
  if (isLoading.value || !isFormValid.value) return

  isLoading.value = true
  error.value = ''
  success.value = ''

  try {
    await apiService.signup(form.username, form.email, form.password)
    
    success.value = 'Account created successfully! Redirecting to login...'
    
    // Redirect to login after a short delay
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err: any) {
    console.error('Signup error:', err)
    error.value = err.response?.data || err.message || 'Signup failed. Please try again.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.signup-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
}

.signup-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 2.5rem;
  width: 100%;
  max-width: 450px;
}

.signup-header {
  text-align: center;
  margin-bottom: 2rem;
}

.signup-header h1 {
  color: #1a202c;
  font-size: 1.875rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.signup-header p {
  color: #718096;
  font-size: 0.875rem;
}

.signup-form {
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
  color: #374151;
  font-size: 0.875rem;
  font-weight: 500;
}

.form-group input {
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input:disabled {
  background-color: #f9fafb;
  cursor: not-allowed;
}

.help-text {
  color: #6b7280;
  font-size: 0.75rem;
}

.error-message {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
}

.success-message {
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
}

.signup-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.signup-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.signup-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.signup-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.signup-footer p {
  color: #6b7280;
  font-size: 0.875rem;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .signup-card {
    padding: 2rem 1.5rem;
  }
  
  .signup-header h1 {
    font-size: 1.5rem;
  }
}
</style>
