package com.redligot.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Photo entity representing an uploaded image with metadata.
 * Each photo is associated with a user for ownership and security purposes.
 */
@Entity
@Table(name = "photos")
public class Photo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Column(length = 255)
	private String title;

	@Size(max = 500)
	@Column(length = 500)
	private String description;

	/**
	 * Original filename provided by the client (optional).
	 */
	@Size(max = 255)
	@Column(length = 255)
	private String originalFilename;

	/**
	 * MIME type of the uploaded file (e.g. image/png).
	 */
	@Size(max = 100)
	@Column(length = 100)
	private String contentType;

	/**
	 * File size in bytes.
	 */
	private Long size;

	/**
	 * Raw image bytes stored directly in the database.
	 * Using BLOB for DB2 compatibility with explicit size.
	 * Excluded from JSON serialization to prevent massive responses.
	 */
	@Lob
	@Column(columnDefinition = "BLOB(10M)")
	@JsonIgnore
	private byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	@JsonIgnore
	private Gallery gallery;

	/**
	 * Timestamp when the photo was created/uploaded.
	 */
	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Gallery getGallery() {
		return gallery;
	}
	
	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
