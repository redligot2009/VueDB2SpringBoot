package com.redligot.backend.dto;

import com.redligot.backend.model.User;
import java.time.LocalDateTime;

public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profilePictureFilename;
    private String profilePictureContentType;
    private Long profilePictureSize;
    private boolean hasProfilePicture;

    public UserProfileDto() {}

    public UserProfileDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.profilePictureFilename = user.getProfilePictureFilename();
        this.profilePictureContentType = user.getProfilePictureContentType();
        this.profilePictureSize = user.getProfilePictureSize();
        this.hasProfilePicture = user.getProfilePictureData() != null && user.getProfilePictureData().length > 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getProfilePictureFilename() {
        return profilePictureFilename;
    }

    public void setProfilePictureFilename(String profilePictureFilename) {
        this.profilePictureFilename = profilePictureFilename;
    }

    public String getProfilePictureContentType() {
        return profilePictureContentType;
    }

    public void setProfilePictureContentType(String profilePictureContentType) {
        this.profilePictureContentType = profilePictureContentType;
    }

    public Long getProfilePictureSize() {
        return profilePictureSize;
    }

    public void setProfilePictureSize(Long profilePictureSize) {
        this.profilePictureSize = profilePictureSize;
    }

    public boolean isHasProfilePicture() {
        return hasProfilePicture;
    }

    public void setHasProfilePicture(boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }
}
