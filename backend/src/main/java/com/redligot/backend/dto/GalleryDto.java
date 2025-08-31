package com.redligot.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GalleryDto {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private int photoCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PhotoDto> previewPhotos;

    // Constructors
    public GalleryDto() {}

    public GalleryDto(Long id, String name, String description, Long userId, int photoCount, 
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.photoCount = photoCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PhotoDto> getPreviewPhotos() {
        return previewPhotos;
    }

    public void setPreviewPhotos(List<PhotoDto> previewPhotos) {
        this.previewPhotos = previewPhotos;
    }
}
