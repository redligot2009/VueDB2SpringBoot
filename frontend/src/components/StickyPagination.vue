<template>
  <div class="sticky-pagination">
    <div class="pagination-section">
      <div class="pagination-info">
        <div class="pagination-stats">
          <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
          <span>{{ totalElements }} total items</span>
        </div>

        <!-- Page Size Selector -->
        <div class="page-size-selector">
          <label for="page-size">Show:</label>
          <select 
            id="page-size" 
            v-model="selectedPageSize" 
            @change="onPageSizeChange" 
            :disabled="loading"
            class="page-size-select"
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
          <span>per page</span>
        </div>
      </div>

              <div class="pagination-controls" v-if="totalPages > 1">
          <button 
            @click="goToFirstPage" 
            :disabled="isFirstPage || loading"
            class="pagination-btn first-btn" 
            title="First Page"
          >
            <font-awesome-icon icon="fa-solid fa-angle-double-left" />
          </button>

          <button 
            @click="goToPreviousPage" 
            :disabled="isFirstPage || loading"
            class="pagination-btn prev-btn" 
            title="Previous Page"
          >
            <font-awesome-icon icon="fa-solid fa-angle-left" />
          </button>

          <div class="page-numbers">
            <button 
              v-for="pageNum in visiblePageNumbers" 
              :key="pageNum" 
              @click="goToPage(pageNum - 1)"
              :class="['page-btn', { active: pageNum - 1 === currentPage }]"
              :disabled="loading"
            >
              {{ pageNum }}
            </button>
          </div>

          <button 
            @click="goToNextPage" 
            :disabled="isLastPage || loading"
            class="pagination-btn next-btn" 
            title="Next Page"
          >
            <font-awesome-icon icon="fa-solid fa-angle-right" />
          </button>

          <button 
            @click="goToLastPage" 
            :disabled="isLastPage || loading"
            class="pagination-btn last-btn" 
            title="Last Page"
          >
            <font-awesome-icon icon="fa-solid fa-angle-double-right" />
          </button>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface Props {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  loading?: boolean
}

interface Emits {
  (e: 'page-change', page: number): void
  (e: 'page-size-change', size: number): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

// Local page size state
const selectedPageSize = ref(props.pageSize)

// Computed properties
const isFirstPage = computed(() => props.currentPage === 0)
const isLastPage = computed(() => props.currentPage === props.totalPages - 1)

const visiblePageNumbers = computed(() => {
  const current = props.currentPage + 1
  const total = props.totalPages
  const maxVisible = 5

  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  let start = Math.max(1, current - Math.floor(maxVisible / 2))
  let end = Math.min(total, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// Methods
const goToPage = (page: number) => {
  if (page >= 0 && page < props.totalPages) {
    emit('page-change', page)
  }
}

const goToFirstPage = () => {
  if (!isFirstPage.value) {
    emit('page-change', 0)
  }
}

const goToLastPage = () => {
  if (!isLastPage.value) {
    emit('page-change', props.totalPages - 1)
  }
}

const goToPreviousPage = () => {
  if (!isFirstPage.value) {
    emit('page-change', props.currentPage - 1)
  }
}

const goToNextPage = () => {
  if (!isLastPage.value) {
    emit('page-change', props.currentPage + 1)
  }
}

const onPageSizeChange = () => {
  emit('page-size-change', selectedPageSize.value)
}

// Watch for prop changes to update local state
watch(() => props.pageSize, (newSize) => {
  selectedPageSize.value = newSize
})
</script>

<style scoped>
/* Sticky Pagination Styles */
.sticky-pagination {
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

.pagination-section {
  padding: 1.5rem 2rem;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 0;
  width: 100%;
  max-width: 100%;
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  color: #4a5568;
  font-size: 0.9rem;
  font-weight: 500;
}

.pagination-stats {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.page-size-selector label {
  color: #4a5568;
  font-weight: 500;
}

.page-size-select {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0.25rem 0.5rem;
  font-size: 0.9rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-size-select:hover:not(:disabled) {
  border-color: #9ca3af;
}

.page-size-select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.page-size-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.pagination-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pagination-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.page-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 2.5rem;
  text-align: center;
}

.page-btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
  transform: translateY(-1px);
}

.page-btn.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.page-btn.active:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

/* Responsive Design */
@media (max-width: 768px) {
  .pagination-section {
    padding: 1rem;
  }
  
  .pagination-info {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .pagination-stats {
    justify-content: center;
  }
  
  .page-size-selector {
    justify-content: center;
  }
  
  .pagination-controls {
    flex-direction: row;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 0.25rem;
  }
  
  .page-numbers {
    justify-content: center;
    order: 2;
  }
  
  .first-btn,
  .prev-btn {
    order: 1;
  }
  
  .next-btn,
  .last-btn {
    order: 3;
  }
  
  .pagination-btn {
    padding: 0.4rem 0.6rem;
    font-size: 0.85rem;
  }
  
  .page-btn {
    padding: 0.4rem 0.6rem;
    font-size: 0.85rem;
    min-width: 2rem;
  }
}

@media (max-width: 480px) {
  .pagination-section {
    padding: 0.75rem;
  }
  
  .pagination-controls {
    gap: 0.2rem;
  }
  
  .pagination-btn {
    padding: 0.35rem 0.5rem;
    font-size: 0.8rem;
  }
  
  .page-btn {
    padding: 0.35rem 0.5rem;
    font-size: 0.8rem;
    min-width: 1.8rem;
  }
  
  .page-numbers {
    gap: 0.2rem;
  }
}
</style>
