package com.redligot.backend.repository;

import com.redligot.backend.model.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    
    List<Gallery> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Gallery> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Optional<Gallery> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    @Query("SELECT g, COUNT(p) as photoCount FROM Gallery g LEFT JOIN g.photos p WHERE g.user.id = :userId GROUP BY g ORDER BY g.createdAt DESC")
    List<Object[]> findGalleriesWithPhotoCountByUserId(@Param("userId") Long userId);
    
    @Query("SELECT g FROM Gallery g LEFT JOIN FETCH g.photos p WHERE g.id = :galleryId AND g.user.id = :userId ORDER BY p.id ASC")
    Optional<Gallery> findByIdAndUserIdWithFirstPhotos(@Param("galleryId") Long galleryId, @Param("userId") Long userId);
    
    @Query("SELECT g FROM Gallery g LEFT JOIN FETCH g.photos p WHERE g.user.id = :userId ORDER BY g.createdAt DESC, p.id ASC")
    List<Gallery> findGalleriesWithPhotosByUserId(@Param("userId") Long userId);
}
