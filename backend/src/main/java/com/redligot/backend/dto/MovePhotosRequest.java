package com.redligot.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class MovePhotosRequest {
    
    @NotEmpty(message = "Photo IDs list cannot be empty")
    private List<Long> photoIds;
    
    // targetGalleryId can be null for unorganized photos
    private Long targetGalleryId;
    
    // Constructors
    public MovePhotosRequest() {}
    
    public MovePhotosRequest(List<Long> photoIds, Long targetGalleryId) {
        this.photoIds = photoIds;
        this.targetGalleryId = targetGalleryId;
    }
    
    // Getters and Setters
    public List<Long> getPhotoIds() {
        return photoIds;
    }
    
    public void setPhotoIds(List<Long> photoIds) {
        this.photoIds = photoIds;
    }
    
    public Long getTargetGalleryId() {
        return targetGalleryId;
    }
    
    public void setTargetGalleryId(Long targetGalleryId) {
        this.targetGalleryId = targetGalleryId;
    }
}
