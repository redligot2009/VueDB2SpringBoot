package com.redligot.backend.service;

import com.redligot.backend.dto.CreateGalleryRequest;
import com.redligot.backend.dto.GalleryDto;
import com.redligot.backend.dto.MovePhotosRequest;
import com.redligot.backend.dto.PhotoDto;
import com.redligot.backend.model.Gallery;
import com.redligot.backend.model.Photo;
import com.redligot.backend.model.User;
import com.redligot.backend.repository.GalleryRepository;
import com.redligot.backend.repository.PhotoRepository;
import com.redligot.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GalleryService {
    
    @Autowired
    private GalleryRepository galleryRepository;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public GalleryDto createGallery(CreateGalleryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (galleryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new RuntimeException("Gallery with this name already exists");
        }
        
        Gallery gallery = new Gallery(request.getName(), request.getDescription(), user);
        Gallery savedGallery = galleryRepository.save(gallery);
        
        return convertToDto(savedGallery);
    }
    
    public List<GalleryDto> getUserGalleries(Long userId) {
        List<Object[]> galleriesWithCounts = galleryRepository.findGalleriesWithPhotoCountByUserId(userId);
        
        return galleriesWithCounts.stream()
                .map(result -> {
                    Gallery gallery = (Gallery) result[0];
                    Long photoCount = (Long) result[1];
                    
                    GalleryDto dto = convertToDto(gallery);
                    dto.setPhotoCount(photoCount.intValue());
                    
                    // Get first 4 photos for preview using a separate query
                    List<Photo> previewPhotos = photoRepository.findFirst4ByGalleryOrderByIdAsc(gallery);
                    List<PhotoDto> previewPhotoDtos = previewPhotos.stream()
                            .map(this::convertPhotoToDto)
                            .collect(Collectors.toList());
                    
                    dto.setPreviewPhotos(previewPhotoDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public Page<GalleryDto> getUserGalleries(Long userId, Pageable pageable) {
        Page<Gallery> galleries = galleryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return galleries.map(this::convertToDto);
    }
    
    public GalleryDto getGallery(Long galleryId, Long userId) {
        Gallery gallery = galleryRepository.findByIdAndUserId(galleryId, userId)
                .orElseThrow(() -> new RuntimeException("Gallery not found"));
        
        return convertToDtoWithPhotos(gallery);
    }
    
    public GalleryDto getGalleryPreview(Long galleryId, Long userId) {
        Gallery gallery = galleryRepository.findByIdAndUserIdWithFirstPhotos(galleryId, userId)
                .orElseThrow(() -> new RuntimeException("Gallery not found"));
        
        GalleryDto dto = convertToDto(gallery);
        
        // Get first 4 photos for preview
        List<PhotoDto> previewPhotos = gallery.getPhotos().stream()
                .limit(4)
                .map(this::convertPhotoToDto)
                .collect(Collectors.toList());
        
        dto.setPreviewPhotos(previewPhotos);
        return dto;
    }
    
    public GalleryDto updateGallery(Long galleryId, CreateGalleryRequest request, Long userId) {
        Gallery gallery = galleryRepository.findByIdAndUserId(galleryId, userId)
                .orElseThrow(() -> new RuntimeException("Gallery not found"));
        
        // Check if name is already taken by another gallery
        if (!gallery.getName().equals(request.getName()) && 
            galleryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new RuntimeException("Gallery with this name already exists");
        }
        
        gallery.setName(request.getName());
        gallery.setDescription(request.getDescription());
        
        Gallery updatedGallery = galleryRepository.save(gallery);
        return convertToDto(updatedGallery);
    }
    
    public void deleteGallery(Long galleryId, Long userId, boolean deletePhotos) {
        Gallery gallery = galleryRepository.findByIdAndUserId(galleryId, userId)
                .orElseThrow(() -> new RuntimeException("Gallery not found"));
        
        if (deletePhotos) {
            // Delete all photos in the gallery
            photoRepository.deleteAll(gallery.getPhotos());
        } else {
            // Move photos to unorganized (set gallery to null)
            gallery.getPhotos().forEach(photo -> photo.setGallery(null));
            photoRepository.saveAll(gallery.getPhotos());
        }
        
        galleryRepository.delete(gallery);
    }
    
    public void movePhotos(MovePhotosRequest request, Long userId) {
        // Verify all photos belong to the user
        List<Photo> photos = photoRepository.findAllById(request.getPhotoIds());
        photos.forEach(photo -> {
            if (!photo.getUser().getId().equals(userId)) {
                throw new RuntimeException("Photo does not belong to user");
            }
        });
        
        // Get target gallery if specified
        final Gallery targetGallery;
        if (request.getTargetGalleryId() != null) {
            targetGallery = galleryRepository.findByIdAndUserId(request.getTargetGalleryId(), userId)
                    .orElseThrow(() -> new RuntimeException("Target gallery not found"));
        } else {
            targetGallery = null;
        }
        
        // Move photos
        photos.forEach(photo -> photo.setGallery(targetGallery));
        photoRepository.saveAll(photos);
    }
    
    public List<GalleryDto> getGalleriesForDropdown(Long userId) {
        return galleryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private GalleryDto convertToDto(Gallery gallery) {
        GalleryDto dto = new GalleryDto();
        dto.setId(gallery.getId());
        dto.setName(gallery.getName());
        dto.setDescription(gallery.getDescription());
        dto.setUserId(gallery.getUser().getId());
        dto.setPhotoCount(gallery.getPhotoCount());
        dto.setCreatedAt(gallery.getCreatedAt());
        dto.setUpdatedAt(gallery.getUpdatedAt());
        return dto;
    }
    
    private GalleryDto convertToDtoWithPhotos(Gallery gallery) {
        GalleryDto dto = convertToDto(gallery);
        
        List<PhotoDto> photoDtos = gallery.getPhotos().stream()
                .map(this::convertPhotoToDto)
                .collect(Collectors.toList());
        
        dto.setPreviewPhotos(photoDtos);
        return dto;
    }
    
    private PhotoDto convertPhotoToDto(Photo photo) {
        PhotoDto dto = new PhotoDto();
        dto.setId(photo.getId());
        dto.setTitle(photo.getTitle());
        dto.setDescription(photo.getDescription());
        dto.setOriginalFilename(photo.getOriginalFilename());
        dto.setContentType(photo.getContentType());
        dto.setSize(photo.getSize());
        dto.setUserId(photo.getUser().getId());
        dto.setGalleryId(photo.getGallery() != null ? photo.getGallery().getId() : null);
        return dto;
    }
}
