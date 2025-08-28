package com.redligot.backend.service;

import com.redligot.backend.model.Photo;
import com.redligot.backend.repository.PhotoRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
	 * Retrieve all photos.
	 *
	 * @return list of all photos
	 */
	public List<Photo> findAll() {
		return photoRepository.findAll();
	}

	/**
	 * Find a photo by its ID.
	 *
	 * @param id photo identifier
	 * @return photo if found
	 * @throws ResponseStatusException if photo not found
	 */
	public Photo findById(Long id) {
		return photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id));
	}

	/**
	 * Create a new photo from uploaded file and metadata.
	 *
	 * @param title       photo title
	 * @param description optional description
	 * @param file        uploaded image file
	 * @return created photo
	 * @throws IOException when reading file fails
	 */
	public Photo create(String title, String description, MultipartFile file) throws IOException {
		Photo photo = new Photo();
		photo.setTitle(title);
		photo.setDescription(description);
		photo.setOriginalFilename(file.getOriginalFilename());
		photo.setContentType(file.getContentType());
		photo.setSize(file.getSize());
		photo.setData(file.getBytes());
		return photoRepository.save(photo);
	}

	/**
	 * Update an existing photo.
	 *
	 * @param id          photo identifier
	 * @param title       new title
	 * @param description new description
	 * @param file        new image file (optional)
	 * @return updated photo
	 * @throws IOException when reading file fails
	 */
	public Photo update(Long id, String title, String description, MultipartFile file) throws IOException {
		Photo existing = findById(id);
		existing.setTitle(title);
		existing.setDescription(description);
		
		if (file != null && !file.isEmpty()) {
			existing.setOriginalFilename(file.getOriginalFilename());
			existing.setContentType(file.getContentType());
			existing.setSize(file.getSize());
			existing.setData(file.getBytes());
		}
		
		return photoRepository.save(existing);
	}

	/**
	 * Delete a photo by ID.
	 *
	 * @param id photo identifier
	 * @throws ResponseStatusException if photo not found
	 */
	public void deleteById(Long id) {
		if (!photoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id);
		}
		photoRepository.deleteById(id);
	}

	/**
	 * Get photo metadata without image data.
	 *
	 * @param id photo identifier
	 * @return photo metadata
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
	 * Get photo image data as a resource.
	 *
	 * @param id photo identifier
	 * @return image resource
	 */
	public Resource getImageResource(Long id) {
		Photo photo = findById(id);
		if (photo.getData() == null || photo.getData().length == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No image data stored for photo id " + id);
		}
		return new ByteArrayResource(photo.getData());
	}

	/**
	 * Lightweight metadata DTO for clients that do not need image bytes.
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
