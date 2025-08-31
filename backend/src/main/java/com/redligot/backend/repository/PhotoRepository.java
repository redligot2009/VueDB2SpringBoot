package com.redligot.backend.repository;

import com.redligot.backend.model.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for {@link Photo} entities, providing CRUD operations.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByUserId(Long userId, Pageable pageable);
    Page<Photo> findByUserIdAndGalleryId(Long userId, Long galleryId, Pageable pageable);
    Page<Photo> findByUserIdAndGalleryIsNull(Long userId, Pageable pageable);
    List<Photo> findFirst4ByGalleryOrderByIdAsc(com.redligot.backend.model.Gallery gallery);
}
