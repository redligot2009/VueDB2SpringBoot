package com.redligot.backend.controller;

import com.redligot.backend.dto.PhotoDto;
import com.redligot.backend.model.Photo;
import com.redligot.backend.model.User;
import com.redligot.backend.security.CustomUserDetails;
import com.redligot.backend.service.PhotoService;
import com.redligot.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for photo management operations.
 */
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photos", description = "Photo management APIs")
public class PhotoController {

	private final PhotoService photoService;
	private final UserService userService;

	public PhotoController(PhotoService photoService, UserService userService) {
		this.photoService = photoService;
		this.userService = userService;
	}

	/**
	 * Response DTO for paginated photo results.
	 */
	public record PaginatedPhotoResponse(
			List<PhotoDto> content,
			long totalElements,
			int totalPages,
			int size,
			int number,
			boolean first,
			boolean last,
			int numberOfElements
	) {
		public static PaginatedPhotoResponse fromPage(Page<Photo> page) {
			List<PhotoDto> photoDtos = page.getContent().stream()
					.map(PhotoDto::new)
					.collect(Collectors.toList());
			
			return new PaginatedPhotoResponse(
					photoDtos,
					page.getTotalElements(),
					page.getTotalPages(),
					page.getSize(),
					page.getNumber(),
					page.isFirst(),
					page.isLast(),
					page.getNumberOfElements()
			);
		}
	}

	/**
	 * List all photos metadata for the authenticated user with pagination support.
	 * Users can only see their own photos.
	 *
	 * @param page page number (0-based, default: 0)
	 * @param size page size (default: 10)
	 * @param userDetails authenticated user details
	 * @return paginated list of {@link Photo} for the authenticated user
	 */
	@GetMapping
	@Operation(summary = "List photos", description = "Get paginated list of photos for the authenticated user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Photos retrieved successfully", 
					content = @Content(schema = @Schema(implementation = PaginatedPhotoResponse.class))),
		@ApiResponse(responseCode = "401", description = "Not authenticated")
	})
	public PaginatedPhotoResponse list(
			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Gallery ID (optional - null for unorganized photos)") @RequestParam(required = false) Long galleryId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Photo> photoPage = photoService.findByUserIdAndGalleryId(userDetails.getId(), galleryId, pageable);
		return PaginatedPhotoResponse.fromPage(photoPage);
	}

	/**
	 * Fetch a single photo metadata by id.
	 * Users can only access their own photos.
	 *
	 * @param id photo identifier
	 * @return 200 with {@link Photo} if found and owned by authenticated user, otherwise 404 is thrown
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Get photo by ID", description = "Get a specific photo by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Photo retrieved successfully", 
					content = @Content(schema = @Schema(implementation = PhotoDto.class))),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "Photo does not belong to user"),
		@ApiResponse(responseCode = "404", description = "Photo not found")
	})
	public ResponseEntity<PhotoDto> get(
			@Parameter(description = "Photo ID", example = "1") @PathVariable Long id, 
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Photo photo = photoService.findById(id);
		// Check if the photo belongs to the authenticated user
		if (!photo.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(new PhotoDto(photo));
	}

	/**
	 * Create a new photo record by uploading an image along with basic metadata.
	 * The photo will be associated with the authenticated user.
	 *
	 * Expected multipart parts:
	 * - title: required
	 * - description: optional
	 * - file: required (image file)
	 *
	 * @param title       photo title
	 * @param description optional description
	 * @param file        uploaded image file
	 * @param userDetails authenticated user details
	 * @return created {@link Photo} associated with the authenticated user
	 * @throws IOException when reading the uploaded file fails
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload photo", description = "Upload a single photo with metadata")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Photo uploaded successfully", 
					content = @Content(schema = @Schema(implementation = Photo.class))),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "400", description = "Invalid file or metadata")
	})
	public ResponseEntity<Photo> create(
			@Parameter(description = "Photo title") @RequestPart("title") String title,
			@Parameter(description = "Photo description (optional)") @RequestPart(value = "description", required = false) String description,
			@Parameter(description = "Image file") @RequestPart("file") MultipartFile file,
			@Parameter(description = "Gallery ID (optional)") @RequestParam(value = "galleryId", required = false) Long galleryId,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
		User user = userService.getCurrentUser(userDetails.getId());
		Photo saved = photoService.create(title, description, file, user, galleryId);
		return ResponseEntity.ok(saved);
	}

	/**
	 * Bulk upload multiple photos at once.
	 * All photos will be associated with the authenticated user.
	 *
	 * Expected multipart parts:
	 * - files: array of image files
	 * - titles: array of titles (optional, will use filename if not provided)
	 * - descriptions: array of descriptions (optional)
	 *
	 * @param files       array of uploaded image files
	 * @param titles      array of titles (optional)
	 * @param descriptions array of descriptions (optional)
	 * @return list of created {@link Photo} objects associated with the authenticated user
	 * @throws IOException when reading the uploaded files fails
	 */
	@PostMapping(path = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Bulk upload photos", description = "Upload multiple photos at once")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Photos uploaded successfully", 
					content = @Content(schema = @Schema(implementation = Photo.class))),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "400", description = "Invalid files or metadata")
	})
	public ResponseEntity<List<Photo>> bulkCreate(
			@Parameter(description = "Array of image files") @RequestPart("files") MultipartFile[] files,
			@Parameter(description = "Array of titles (optional)") @RequestParam(value = "titles", required = false) String[] titles,
			@Parameter(description = "Array of descriptions (optional)") @RequestParam(value = "descriptions", required = false) String[] descriptions,
			@Parameter(description = "Gallery ID (optional)") @RequestParam(value = "galleryId", required = false) Long galleryId,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
		
		// Debug: Log what we received
		System.out.println("Bulk upload received:");
		System.out.println("- Files count: " + (files != null ? files.length : "null"));
		System.out.println("- Titles count: " + (titles != null ? titles.length : "null"));
		System.out.println("- Descriptions count: " + (descriptions != null ? descriptions.length : "null"));
		
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				System.out.println("- File " + i + ": " + files[i].getOriginalFilename() + 
					" (size: " + files[i].getSize() + ", type: " + files[i].getContentType() + ")");
			}
		}
		
		User user = userService.getCurrentUser(userDetails.getId());
		List<Photo> savedPhotos = photoService.bulkCreate(files, titles, descriptions, user, galleryId);
		return ResponseEntity.ok(savedPhotos);
	}

	/**
	 * Update existing photo metadata and optionally replace the stored image bytes.
	 * Users can only update their own photos.
	 *
	 * @param id          photo identifier
	 * @param title       new title
	 * @param description new description (optional)
	 * @param file        new image file (optional)
	 * @return updated {@link Photo} if owned by authenticated user
	 * @throws IOException when reading the uploaded file fails
	 */
	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Update photo", description = "Update photo metadata and optionally replace the image file")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Photo updated successfully", 
					content = @Content(schema = @Schema(implementation = Photo.class))),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "Photo does not belong to user"),
		@ApiResponse(responseCode = "404", description = "Photo not found"),
		@ApiResponse(responseCode = "400", description = "Invalid data")
	})
	public ResponseEntity<Photo> update(
			@Parameter(description = "Photo ID", example = "1") @PathVariable Long id,
			@Parameter(description = "New photo title") @RequestPart("title") String title,
			@Parameter(description = "New photo description (optional)") @RequestPart(value = "description", required = false) String description,
			@Parameter(description = "New image file (optional)") @RequestPart(value = "file", required = false) MultipartFile file,
			@Parameter(description = "Gallery ID (optional)") @RequestParam(value = "galleryId", required = false) Long galleryId,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
		Photo photo = photoService.findById(id);
		// Check if the photo belongs to the authenticated user
		if (!photo.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(403).build();
		}
		Photo updated = photoService.update(id, title, description, file, galleryId);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a photo by id.
	 * Users can only delete their own photos.
	 *
	 * @param id photo identifier
	 * @return 204 if deleted and owned by authenticated user, 404 if not found
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete photo", description = "Delete a photo by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "Photo does not belong to user"),
		@ApiResponse(responseCode = "404", description = "Photo not found")
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Photo ID", example = "1") @PathVariable Long id, 
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Photo photo = photoService.findById(id);
		// Check if the photo belongs to the authenticated user
		if (!photo.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(403).build();
		}
		photoService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Bulk delete multiple photos by their ids.
	 * Users can only delete their own photos.
	 *
	 * @param ids list of photo identifiers
	 * @param userDetails authenticated user details
	 * @return 204 if all photos were deleted successfully
	 */
	@DeleteMapping("/bulk")
	@Operation(summary = "Bulk delete photos", description = "Delete multiple photos by their IDs")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Photos deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "One or more photos do not belong to user"),
		@ApiResponse(responseCode = "404", description = "One or more photos not found")
	})
	public ResponseEntity<Void> bulkDelete(
			@Parameter(description = "List of photo IDs to delete") @RequestBody List<Long> ids,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		// Verify all photos belong to the authenticated user
		for (Long id : ids) {
			Photo photo = photoService.findById(id);
			if (!photo.getUser().getId().equals(userDetails.getId())) {
				return ResponseEntity.status(403).build();
			}
		}
		
		// Delete all photos
		photoService.bulkDeleteByIds(ids);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Fetch only the photo metadata (no image bytes) by id.
	 * Users can only access their own photos.
	 *
	 * @param id photo identifier
	 * @return selected metadata fields as JSON if owned by authenticated user
	 */
	@GetMapping("/{id}/metadata")
	@Operation(summary = "Get photo metadata", description = "Get photo metadata without image bytes")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Metadata retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "Photo does not belong to user"),
		@ApiResponse(responseCode = "404", description = "Photo not found")
	})
	public ResponseEntity<PhotoService.PhotoMetadata> getMetadata(
			@Parameter(description = "Photo ID", example = "1") @PathVariable Long id, 
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Photo photo = photoService.findById(id);
		// Check if the photo belongs to the authenticated user
		if (!photo.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(403).build();
		}
		PhotoService.PhotoMetadata meta = photoService.getMetadata(id);
		return ResponseEntity.ok(meta);
	}

	/**
	 * Download the raw image bytes for a photo.
	 * Users can only download their own photos.
	 *
	 * @param id photo identifier
	 * @return image stream with content type and filename if owned by authenticated user
	 */
	@GetMapping("/{id}/file")
	@Operation(summary = "Download photo file", description = "Download the raw image file for a photo")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Image file downloaded successfully"),
		@ApiResponse(responseCode = "401", description = "Not authenticated"),
		@ApiResponse(responseCode = "403", description = "Photo does not belong to user"),
		@ApiResponse(responseCode = "404", description = "Photo not found")
	})
	public ResponseEntity<Resource> download(
			@Parameter(description = "Photo ID", example = "1") @PathVariable Long id, 
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		Photo photo = photoService.findById(id);
		// Check if the photo belongs to the authenticated user
		if (!photo.getUser().getId().equals(userDetails.getId())) {
			return ResponseEntity.status(403).build();
		}
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
