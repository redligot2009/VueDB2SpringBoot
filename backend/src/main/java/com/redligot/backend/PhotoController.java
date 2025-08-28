package com.redligot.backend;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

	private final PhotoRepository photoRepository;

	public PhotoController(PhotoRepository photoRepository) {
		this.photoRepository = photoRepository;
	}

	/**
	 * List all photos metadata stored in the database.
	 *
	 * @return list of {@link Photo}
	 */
	@GetMapping
	public List<Photo> list() {
		return photoRepository.findAll();
	}

	/**
	 * Fetch a single photo metadata by id.
	 *
	 * @param id photo identifier
	 * @return 200 with {@link Photo} if found, otherwise 404 is thrown
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Photo> get(@PathVariable Long id) {
		Photo p = photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id));
		return ResponseEntity.ok(p);
	}

	/**
	 * Fetch only the photo metadata (no image bytes) by id.
	 *
	 * @param id photo identifier
	 * @return selected metadata fields as JSON
	 */
	@GetMapping("/{id}/metadata")
	public ResponseEntity<PhotoMetadata> getMetadata(@PathVariable Long id) {
		Photo p = photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id));
		PhotoMetadata meta = new PhotoMetadata(
				p.getId(),
				p.getTitle(),
				p.getDescription(),
				p.getOriginalFilename(),
				p.getContentType(),
				p.getSize()
		);
		return ResponseEntity.ok(meta);
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
		Photo photo = new Photo();
		photo.setTitle(title);
		photo.setDescription(description);
		photo.setOriginalFilename(file.getOriginalFilename());
		photo.setContentType(file.getContentType());
		photo.setSize(file.getSize());
		photo.setData(file.getBytes());
		Photo saved = photoRepository.save(photo);
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
		Photo existing = photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id));
		existing.setTitle(title);
		existing.setDescription(description);
		if (file != null && !file.isEmpty()) {
			try {
				existing.setOriginalFilename(file.getOriginalFilename());
				existing.setContentType(file.getContentType());
				existing.setSize(file.getSize());
				existing.setData(file.getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return ResponseEntity.ok(photoRepository.save(existing));
	}

	/**
	 * Delete a photo by id.
	 *
	 * @param id photo identifier
	 * @return 204 if deleted, 404 if not found
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!photoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id);
		}
		photoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Download the raw image bytes for a photo.
	 *
	 * @param id photo identifier
	 * @return image stream with content type and filename
	 */
	@GetMapping("/{id}/file")
	public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
		Photo p = photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found with id " + id));
		if (p.getData() == null || p.getData().length == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No image data stored for photo id " + id);
		}
		ByteArrayResource resource = new ByteArrayResource(p.getData());
		String filename = p.getOriginalFilename() != null ? p.getOriginalFilename() : ("photo-" + p.getId());
		MediaType type = p.getContentType() != null ? MediaType.parseMediaType(p.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(type)
				.contentLength(p.getSize() != null ? p.getSize() : p.getData().length)
				.body(resource);
	}
}
