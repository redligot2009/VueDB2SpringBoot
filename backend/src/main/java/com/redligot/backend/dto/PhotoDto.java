package com.redligot.backend.dto;

import com.redligot.backend.model.Photo;

/**
 * DTO for Photo responses that excludes the image data to prevent massive responses.
 * Used for listing photos without sending the full Base64 image data.
 */
public class PhotoDto {
    private Long id;
    private String title;
    private String description;
    private String originalFilename;
    private String contentType;
    private Long size;
    private Long userId;
    private Long galleryId;

    public PhotoDto() {}

    public PhotoDto(Photo photo) {
        this.id = photo.getId();
        this.title = photo.getTitle();
        this.description = photo.getDescription();
        this.originalFilename = photo.getOriginalFilename();
        this.contentType = photo.getContentType();
        this.size = photo.getSize();
        this.userId = photo.getUser().getId();
        this.galleryId = photo.getGallery() != null ? photo.getGallery().getId() : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getGalleryId() {
        return galleryId;
    }
    
    public void setGalleryId(Long galleryId) {
        this.galleryId = galleryId;
    }
}
