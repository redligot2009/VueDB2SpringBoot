package com.redligot.backend.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for user profile information returned by the /me endpoint
 */
public class UserProfile {
    
    private Long id;
    private String username;
    private String email;
    
    @JsonProperty("hasProfilePicture")
    private boolean hasProfilePicture;
    
    private String profilePictureFilename;
    private String profilePictureContentType;
    private Long profilePictureSize;

    // Default constructor
    public UserProfile() {}

    // Constructor with fields
    public UserProfile(Long id, String username, String email, boolean hasProfilePicture, 
                      String profilePictureFilename, String profilePictureContentType, Long profilePictureSize) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.hasProfilePicture = hasProfilePicture;
        this.profilePictureFilename = profilePictureFilename;
        this.profilePictureContentType = profilePictureContentType;
        this.profilePictureSize = profilePictureSize;
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

    public boolean isHasProfilePicture() {
        return hasProfilePicture;
    }

    public void setHasProfilePicture(boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
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
}
