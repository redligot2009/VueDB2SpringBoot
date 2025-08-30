package com.redligot.backend.service;

import com.redligot.backend.model.Photo;
import com.redligot.backend.repository.PhotoRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Service layer for photo business logic and operations.
 */
@Service
public class PhotoService {

	private final PhotoRepository photoRepository;

	public PhotoService(PhotoRepository photoRepository) {
		this.photoRepository = photoRepository;
	}

	/**
	 * Retrieve all photos from the database.
	 * Note: This method returns all photos regardless of user ownership.
	 * Use {@link #findByUserId(Long, Pageable)} for user-specific photos.
	 * 
	 * @return List of all photos
	 */
	public List<Photo> findAll() {
		return photoRepository.findAll();
	}

	/**
	 * Retrieve all photos from the database with pagination support.
	 * Note: This method returns all photos regardless of user ownership.
	 * Use {@link #findByUserId(Long, Pageable)} for user-specific photos.
	 * 
	 * @param pageable pagination parameters
	 * @return Page of photos
	 */
	public Page<Photo> findAll(Pageable pageable) {
		return photoRepository.findAll(pageable);
	}

	/**
	 * Retrieve all photos for a specific user with pagination support.
	 * This method is used to filter photos by user ownership for security.
	 * 
	 * @param userId The user ID to filter photos by
	 * @param pageable pagination parameters
	 * @return Page of photos belonging to the specified user
	 */
	public Page<Photo> findByUserId(Long userId, Pageable pageable) {
		return photoRepository.findByUserId(userId, pageable);
	}

	/**
	 * Find a photo by its ID.
	 * 
	 * @param id The photo ID
	 * @return The photo if found
	 * @throws ResponseStatusException if photo not found
	 */
	public Photo findById(Long id) {
		return photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
						"Photo with ID " + id + " not found"));
	}

	/**
	 * Create a new photo from uploaded file and metadata.
	 * The photo will be associated with the specified user for ownership tracking.
	 * 
	 * @param title Photo title
	 * @param description Photo description (optional)
	 * @param file Uploaded image file
	 * @param user The user who owns the photo (required for authentication)
	 * @return The created photo with user association
	 * @throws ResponseStatusException if file is invalid or too large
	 */
	public Photo create(String title, String description, MultipartFile file, com.redligot.backend.model.User user) {
		try {
			// Validate file size (max 8MB to fit in DB2 BLOB(10M))
			if (file.getSize() > 8 * 1024 * 1024) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
						"File size exceeds maximum limit of 8MB");
			}

			// Validate file type
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
						"Only image files are allowed");
			}

			Photo photo = new Photo();
			photo.setTitle(title);
			photo.setDescription(description);
			photo.setOriginalFilename(file.getOriginalFilename());
			photo.setContentType(contentType);
			photo.setSize(file.getSize());
			photo.setData(file.getBytes());
			photo.setUser(user);

			return photoRepository.save(photo);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
					"Failed to process uploaded file: " + e.getMessage());
		}
	}

	/**
	 * Bulk create multiple photos from uploaded files and metadata.
	 * All photos will be associated with the specified user for ownership tracking.
	 * 
	 * @param files Array of uploaded image files
	 * @param titles Array of titles (optional, will use filename if not provided)
	 * @param descriptions Array of descriptions (optional)
	 * @param user The user who owns the photos (required for authentication)
	 * @return List of created photos with user association
	 * @throws ResponseStatusException if any file is invalid or too large
	 */
	public List<Photo> bulkCreate(MultipartFile[] files, String[] titles, String[] descriptions, com.redligot.backend.model.User user) {
		if (files == null || files.length == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"At least one file must be provided");
		}

		List<Photo> createdPhotos = new java.util.ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			
			// Use provided title or fallback to filename without extension
			String title = (titles != null && i < titles.length && titles[i] != null && !titles[i].trim().isEmpty()) 
					? titles[i].trim() 
					: getFilenameWithoutExtension(file.getOriginalFilename());
			
			// Use provided description or null
			String description = (descriptions != null && i < descriptions.length) ? descriptions[i] : null;

			try {
				// Validate file size (max 8MB to fit in DB2 BLOB(10M))
				if (file.getSize() > 8 * 1024 * 1024) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
							"File " + file.getOriginalFilename() + " exceeds maximum limit of 8MB");
				}

				// Validate file type
				String contentType = file.getContentType();
				if (contentType == null || !contentType.startsWith("image/")) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
							"File " + file.getOriginalFilename() + " is not a valid image file");
				}

				Photo photo = new Photo();
				photo.setTitle(title);
				photo.setDescription(description);
				photo.setOriginalFilename(file.getOriginalFilename());
				photo.setContentType(contentType);
				photo.setSize(file.getSize());
				photo.setData(file.getBytes());
				photo.setUser(user);

				createdPhotos.add(photoRepository.save(photo));
			} catch (IOException e) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
						"Failed to process uploaded file " + file.getOriginalFilename() + ": " + e.getMessage());
			}
		}

		return createdPhotos;
	}

	/**
	 * Helper method to extract filename without extension.
	 * 
	 * @param filename Original filename
	 * @return Filename without extension
	 */
	private String getFilenameWithoutExtension(String filename) {
		if (filename == null) return "Untitled";
		int lastDotIndex = filename.lastIndexOf('.');
		return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
	}

	/**
	 * Update an existing photo.
	 * 
	 * @param id Photo ID
	 * @param title New title
	 * @param description New description
	 * @param file New image file (optional)
	 * @return The updated photo
	 * @throws ResponseStatusException if photo not found or file is invalid
	 */
	public Photo update(Long id, String title, String description, MultipartFile file) {
		Photo existingPhoto = findById(id);

		existingPhoto.setTitle(title);
		existingPhoto.setDescription(description);

		if (file != null && !file.isEmpty()) {
			try {
				// Validate file size (max 8MB to fit in DB2 BLOB(10M))
				if (file.getSize() > 8 * 1024 * 1024) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
							"File size exceeds maximum limit of 8MB");
				}

				// Validate file type
				String contentType = file.getContentType();
				if (contentType == null || !contentType.startsWith("image/")) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
							"Only image files are allowed");
				}

				existingPhoto.setOriginalFilename(file.getOriginalFilename());
				existingPhoto.setContentType(contentType);
				existingPhoto.setSize(file.getSize());
				existingPhoto.setData(file.getBytes());
			} catch (IOException e) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
						"Failed to process uploaded file: " + e.getMessage());
			}
		}

		return photoRepository.save(existingPhoto);
	}

	/**
	 * Delete a photo by ID.
	 * 
	 * @param id Photo ID
	 * @throws ResponseStatusException if photo not found
	 */
	public void deleteById(Long id) {
		if (!photoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
					"Photo with ID " + id + " not found");
		}
		photoRepository.deleteById(id);
	}

	/**
	 * Bulk delete multiple photos by their IDs.
	 * 
	 * @param ids List of photo IDs to delete
	 * @throws ResponseStatusException if any photo not found
	 */
	public void bulkDeleteByIds(List<Long> ids) {
		// Verify all photos exist before deleting
		for (Long id : ids) {
			if (!photoRepository.existsById(id)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
						"Photo with ID " + id + " not found");
			}
		}
		photoRepository.deleteAllById(ids);
	}

	/**
	 * Get photo metadata without the image data.
	 * 
	 * @param id Photo ID
	 * @return Photo metadata
	 * @throws ResponseStatusException if photo not found
	 */
	public PhotoMetadata getMetadata(Long id) {
		Photo photo = findById(id);
		return new PhotoMetadata(
				photo.getId(),
				photo.getTitle(),
				photo.getDescription(),
				photo.getOriginalFilename(),
				photo.getContentType(),
				photo.getSize()
		);
	}

	/**
	 * Get the image data as a resource for download/display.
	 * 
	 * @param id Photo ID
	 * @return ByteArrayResource containing the image data
	 * @throws ResponseStatusException if photo not found
	 */
	public Resource getImageResource(Long id) {
		Photo photo = findById(id);
		if (photo.getData() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
					"Image data not found for photo with ID " + id);
		}
		return new ByteArrayResource(photo.getData());
	}

	/**
	 * Record class for photo metadata without image data.
	 */
	public record PhotoMetadata(
			Long id,
			String title,
			String description,
			String originalFilename,
			String contentType,
			Long size
	) {}
}
