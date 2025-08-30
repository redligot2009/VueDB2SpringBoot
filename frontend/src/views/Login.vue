<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>Welcome Back</h1>
        <p>Sign in to your account to continue</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="usernameOrEmail">Username or Email</label>
          <input
            id="usernameOrEmail"
            v-model="form.usernameOrEmail"
            type="text"
            required
            placeholder="Enter your username or email"
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
            placeholder="Enter your password"
            :disabled="isLoading"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="login-button" :disabled="isLoading">
          <span v-if="isLoading" class="loading-spinner"></span>
          {{ isLoading ? 'Signing In...' : 'Sign In' }}
        </button>
      </form>

      <div class="login-footer">
        <p>
          Don't have an account?
          <router-link to="/signup" class="link">Sign up here</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { apiService } from '@/services/api'
import { getUserIdFromToken } from '@/utils/jwt'

const router = useRouter()
const authStore = useAuthStore()

const isLoading = ref(false)
const error = ref('')

const form = reactive({
  usernameOrEmail: '',
  password: ''
})

const handleLogin = async () => {
  if (isLoading.value) return

  isLoading.value = true
  error.value = ''

  try {
    const response = await apiService.login(form.usernameOrEmail, form.password)
    
    // Extract user ID from JWT token
    const userId = getUserIdFromToken(response.accessToken)
    if (!userId) {
      throw new Error('Invalid token received from server')
    }
    
    // Create user object with proper ID from JWT
    const user = {
      id: userId,
      username: form.usernameOrEmail,
      email: form.usernameOrEmail.includes('@') ? form.usernameOrEmail : ''
    }
    
    authStore.login(user, response.accessToken)
    
    // Redirect to photo gallery
    router.push('/gallery')
  } catch (err: any) {
    console.error('Login error:', err)
    error.value = err.response?.data || err.message || 'Login failed. Please try again.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
}

.login-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 2.5rem;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  color: #1a202c;
  font-size: 1.875rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.login-header p {
  color: #718096;
  font-size: 0.875rem;
}

.login-form {
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

.error-message {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
}

.login-button {
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

.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-button:disabled {
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

.login-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.login-footer p {
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
  .login-card {
    padding: 2rem 1.5rem;
  }
  
  .login-header h1 {
    font-size: 1.5rem;
  }
}
</style>
