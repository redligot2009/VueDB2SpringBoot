package com.redligot.backend.repository;

import com.redligot.backend.model.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Photo} entities, providing CRUD operations.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByUserId(Long userId, Pageable pageable);
}
