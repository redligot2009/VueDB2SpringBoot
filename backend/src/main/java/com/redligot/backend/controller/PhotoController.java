package com.redligot.backend.controller;

import com.redligot.backend.model.Photo;
import com.redligot.backend.service.PhotoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for photo management operations.
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoController {

	private final PhotoService photoService;

	public PhotoController(PhotoService photoService) {
		this.photoService = photoService;
	}

	/**
	 * List all photos metadata stored in the database.
	 *
	 * @return list of {@link Photo}
	 */
	@GetMapping
	public List<Photo> list() {
		return photoService.findAll();
	}

	/**
	 * Fetch a single photo metadata by id.
	 *
	 * @param id photo identifier
	 * @return 200 with {@link Photo} if found, otherwise 404 is thrown
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Photo> get(@PathVariable Long id) {
		Photo photo = photoService.findById(id);
		return ResponseEntity.ok(photo);
	}

	/**
	 * Create a new photo record by uploading an image along with basic metadata.
	 *
	 * Expected multipart parts:
	 * - title: required
	 * - description: optional
	 * - file: required (image file)
	 *
	 * @param title       photo title
	 * @param description optional description
	 * @param file        uploaded image file
	 * @return created {@link Photo}
	 * @throws IOException when reading the uploaded file fails
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Photo> create(
			@RequestPart("title") String title,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart("file") MultipartFile file) throws IOException {
		Photo saved = photoService.create(title, description, file);
		return ResponseEntity.ok(saved);
	}

	/**
	 * Update existing photo metadata and optionally replace the stored image bytes.
	 *
	 * @param id          photo identifier
	 * @param title       new title
	 * @param description new description (optional)
	 * @param file        new image file (optional)
	 * @return updated {@link Photo}
	 * @throws IOException when reading the uploaded file fails
	 */
	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Photo> update(
			@PathVariable Long id,
			@RequestPart("title") String title,
			@RequestPart(value = "description", required = false) String description,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		Photo updated = photoService.update(id, title, description, file);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a photo by id.
	 *
	 * @param id photo identifier
	 * @return 204 if deleted, 404 if not found
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		photoService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Fetch only the photo metadata (no image bytes) by id.
	 *
	 * @param id photo identifier
	 * @return selected metadata fields as JSON
	 */
	@GetMapping("/{id}/metadata")
	public ResponseEntity<PhotoService.PhotoMetadata> getMetadata(@PathVariable Long id) {
		PhotoService.PhotoMetadata meta = photoService.getMetadata(id);
		return ResponseEntity.ok(meta);
	}

	/**
	 * Download the raw image bytes for a photo.
	 *
	 * @param id photo identifier
	 * @return image stream with content type and filename
	 */
	@GetMapping("/{id}/file")
	public ResponseEntity<Resource> download(@PathVariable Long id) {
		Photo photo = photoService.findById(id);
		Resource resource = photoService.getImageResource(id);
		String filename = photo.getOriginalFilename() != null ? photo.getOriginalFilename() : ("photo-" + photo.getId());
		MediaType type = photo.getContentType() != null ? MediaType.parseMediaType(photo.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(type)
				.contentLength(photo.getSize() != null ? photo.getSize() : photo.getData().length)
				.body(resource);
	}
}
