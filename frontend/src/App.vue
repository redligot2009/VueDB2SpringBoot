<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { ref, onMounted, watch } from 'vue'
import { useModalStore } from '@/stores/modalStore'
import { useAuthStore } from '@/stores/authStore'
import DeleteConfirmModal from '@/components/DeleteConfirmModal.vue'
import GlobalBulkUploadModal from '@/components/GlobalBulkUploadModal.vue'
import GlobalMovePhotosModal from '@/components/GlobalMovePhotosModal.vue'
import GlobalEditGalleryModal from '@/components/GlobalEditGalleryModal.vue'
import GlobalDeleteGalleryModal from '@/components/GlobalDeleteGalleryModal.vue'
import GlobalCreateGalleryModal from '@/components/GlobalCreateGalleryModal.vue'
import UserAvatar from '@/components/UserAvatar.vue'

const router = useRouter()
const isMenuOpen = ref(false)
const modalStore = useModalStore()
const authStore = useAuthStore()

// Avatar key for forcing re-render when profile picture changes
const avatarKey = ref(0)

// Watch for changes in profile picture update time to force avatar re-render
watch(() => authStore.profilePictureUpdateTime, () => {
  avatarKey.value++
})

// Initialize user data on app startup if token exists
onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.refreshUserData()
    } catch (error) {
      console.error('Failed to restore user data on app startup:', error)
      // If we can't restore user data, logout the user
      authStore.logout()
      router.push('/login')
    }
  }
})

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

const handleLogout = () => {
  authStore.logout()
  closeMenu()
  // Redirect to login page after logout
  router.push('/login')
}
</script>

<template>
  <div id="app">
    <header class="app-header">
      <div class="header-wrapper">
        <div class="header-content">
          <RouterLink to="/photos" class="app-title-link">
            <h1 class="app-title">Photo Gallery</h1>
          </RouterLink>
          
          <!-- Mobile menu button -->
          <button class="mobile-menu-btn" @click="toggleMenu" :class="{ 'active': isMenuOpen }">
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
          </button>

          <!-- Desktop navigation -->
          <nav class="app-nav desktop-nav" v-if="authStore.isAuthenticated">
            <RouterLink to="/photos" class="nav-link" @click="closeMenu">Photos</RouterLink>
            <RouterLink to="/galleries" class="nav-link" @click="closeMenu">Galleries</RouterLink>
            <RouterLink to="/upload" class="nav-link" @click="closeMenu">Upload</RouterLink>
            <RouterLink to="/profile" class="nav-link" @click="closeMenu">Profile</RouterLink>
            <div class="user-section">
              <UserAvatar 
                :key="`menu-avatar-${avatarKey}`"
                :username="authStore.user?.username || ''" 
                :has-profile-picture="authStore.userProfile?.hasProfilePicture || false"
                size="small"
              />
              <span class="username">{{ authStore.user?.username }}</span>
              <button @click="handleLogout" class="logout-btn">Logout</button>
            </div>
          </nav>
        </div>

        <!-- Mobile navigation -->
        <nav class="app-nav mobile-nav" :class="{ 'open': isMenuOpen }" v-if="authStore.isAuthenticated">
          <RouterLink to="/photos" class="nav-link" @click="closeMenu">Photos</RouterLink>
          <RouterLink to="/galleries" class="nav-link" @click="closeMenu">Galleries</RouterLink>
          <RouterLink to="/upload" class="nav-link" @click="closeMenu">Upload</RouterLink>
          <RouterLink to="/profile" class="nav-link" @click="closeMenu">Profile</RouterLink>
          <div class="mobile-user-section">
            <UserAvatar 
              :key="`mobile-menu-avatar-${avatarKey}`"
              :username="authStore.user?.username || ''" 
              :has-profile-picture="authStore.userProfile?.hasProfilePicture || false"
              size="small"
            />
            <span class="username">{{ authStore.user?.username }}</span>
            <button @click="handleLogout" class="logout-btn">Logout</button>
          </div>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>

    <!-- Global Delete Confirmation Modal -->
    <DeleteConfirmModal
      :is-open="modalStore.deleteModal.isOpen"
      :message="modalStore.deleteModal.message"
      :is-deleting="modalStore.deleteModal.isDeleting"
      @confirm="modalStore.handleConfirm"
      @cancel="modalStore.handleCancel"
    />

    <!-- Global Bulk Upload Modal -->
    <GlobalBulkUploadModal />
    
    <!-- Global Move Photos Modal -->
    <GlobalMovePhotosModal />
    
    <!-- Global Edit Gallery Modal -->
    <GlobalEditGalleryModal />
    
            <!-- Global Delete Gallery Modal -->
        <GlobalDeleteGalleryModal />

        <!-- Global Create Gallery Modal -->
        <GlobalCreateGalleryModal />
  </div>
</template>

<style>
/* Reset and base styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: #1a1a1a;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  width: 100vw;
  max-width: 100%;
}

/* Header styles */
.app-header {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-wrapper {
  position: relative;
  width: 100%;
  max-width: 100%;
  margin: 0;
}

.header-content {
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  position: relative;
  z-index: 1001;
  background-color: white;
}

.app-title-link {
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
}

.app-title-link:hover {
  transform: translateY(-1px);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

/* Desktop navigation */
.desktop-nav {
  display: flex;
  gap: 2rem;
  flex: 1;
  justify-content: center;
  margin-left: 2rem;
}

.nav-link {
  text-decoration: none;
  color: #4a4a4a;
  font-weight: 600;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  font-size: 1rem;
}

.nav-link:hover {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
  transform: translateY(-1px);
}

.nav-link.router-link-active {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.15);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

/* Mobile menu button */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  justify-content: space-around;
  width: 30px;
  height: 30px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  z-index: 10;
}

.hamburger-line {
  width: 100%;
  height: 3px;
  background: #1a1a1a;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.mobile-menu-btn.active .hamburger-line:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.mobile-menu-btn.active .hamburger-line:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.active .hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}

/* Mobile navigation */
.mobile-nav {
  display: none;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  padding: 1rem 2rem;
  gap: 0.5rem;
  position: fixed;
  top: 70px; /* Approximate header height */
  left: 0;
  right: 0;
  z-index: 999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  visibility: hidden;
  opacity: 0;
  transform: translateY(-100%);
  transition: all 0.3s ease;
}

.mobile-nav.open {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
}

.mobile-nav .nav-link {
  padding: 1rem;
  border-radius: 8px;
  font-size: 1.1rem;
  text-align: center;
  border: 1px solid transparent;
  position: relative;
  z-index: 1;
}

.mobile-nav .nav-link:hover {
  border-color: rgba(37, 99, 235, 0.2);
  z-index: 1;
}

/* User section styles */
.user-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: 2rem;
}

.username {
  color: #374151;
  font-weight: 500;
  font-size: 0.875rem;
}

.logout-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.logout-btn:hover {
  background: #dc2626;
}

.mobile-user-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin-top: 0.5rem;
}

.mobile-user-section .user-avatar {
  margin-bottom: 0.5rem;
}

.mobile-user-section .username {
  font-size: 1rem;
  font-weight: 600;
}

.mobile-user-section .logout-btn {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
}

/* Main content */
.app-main {
  flex: 1;
  padding: 2rem 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  margin: 0;
  width: 100%;
  max-width: 100%;
  border-radius: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* Responsive design */
@media (max-width: 768px) {
  #app {
    margin: 0;
    padding: 0;
    width: 100vw;
    max-width: 100%;
  }
  
  .header-wrapper {
    margin: 0;
    width: 100%;
    max-width: 100%;
  }
  
  .header-content {
    padding: 1rem;
    gap: 1rem;
  }
  
  .app-title {
    font-size: 1.5rem;
  }
  
  .desktop-nav {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .mobile-nav {
    display: flex !important;
  }
  
  .app-main {
    margin: 0;
    padding: 1.5rem 0;
    border-radius: 0;
  }
}

@media (max-width: 480px) {
  #app {
    margin: 0;
    padding: 0;
    width: 100vw;
    max-width: 100%;
  }
  
  .header-content {
    padding: 0.75rem;
  }
  
  .app-title {
    font-size: 1.3rem;
  }
  
  .mobile-nav {
    padding: 1rem;
    top: 60px; /* Smaller header height on mobile */
  }
  
  .app-main {
    margin: 0;
    padding: 1rem 0;
    border-radius: 0;
  }
}

/* Large screen optimizations */
@media (min-width: 1200px) {
  .desktop-nav {
    gap: 3rem;
    margin-left: 3rem;
  }
  
  .nav-link {
    padding: 0.75rem 1.5rem;
    font-size: 1.1rem;
  }
}
</style>
