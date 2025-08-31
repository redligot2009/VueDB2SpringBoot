package com.redligot.backend.controller;

import com.redligot.backend.dto.CreateGalleryRequest;
import com.redligot.backend.dto.GalleryDto;
import com.redligot.backend.dto.MovePhotosRequest;
import com.redligot.backend.security.CustomUserDetails;
import com.redligot.backend.service.GalleryService;
import com.redligot.backend.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/galleries")
@CrossOrigin(origins = "*")
@Tag(name = "Gallery Management", description = "APIs for managing photo galleries")
public class GalleryController {
    
    @Autowired
    private GalleryService galleryService;
    
    @PostMapping
    @Operation(summary = "Create a new gallery", description = "Create a new gallery for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gallery created successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or gallery name already exists"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GalleryDto> createGallery(
            @Valid @RequestBody CreateGalleryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GalleryDto gallery = galleryService.createGallery(request, userDetails.getId());
        return ResponseEntity.ok(gallery);
    }
    
    @GetMapping
    @Operation(summary = "Get user galleries", description = "Get all galleries for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Galleries retrieved successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<GalleryDto>> getUserGalleries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GalleryDto> galleries = galleryService.getUserGalleries(userDetails.getId());
        return ResponseEntity.ok(galleries);
    }
    
    @GetMapping("/page")
    @Operation(summary = "Get user galleries with pagination", description = "Get paginated galleries for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Galleries retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Page<GalleryDto>> getUserGalleriesPaginated(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GalleryDto> galleries = galleryService.getUserGalleries(userDetails.getId(), pageable);
        return ResponseEntity.ok(galleries);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get gallery by ID", description = "Get a specific gallery with all its photos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gallery retrieved successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "404", description = "Gallery not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GalleryDto> getGallery(
            @Parameter(description = "Gallery ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GalleryDto gallery = galleryService.getGallery(id, userDetails.getId());
        return ResponseEntity.ok(gallery);
    }
    
    @GetMapping("/{id}/preview")
    @Operation(summary = "Get gallery preview", description = "Get gallery with first 4 photos for preview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gallery preview retrieved successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "404", description = "Gallery not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GalleryDto> getGalleryPreview(
            @Parameter(description = "Gallery ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GalleryDto gallery = galleryService.getGalleryPreview(id, userDetails.getId());
        return ResponseEntity.ok(gallery);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update gallery", description = "Update an existing gallery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gallery updated successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "404", description = "Gallery not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or gallery name already exists"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GalleryDto> updateGallery(
            @Parameter(description = "Gallery ID") @PathVariable Long id,
            @Valid @RequestBody CreateGalleryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GalleryDto gallery = galleryService.updateGallery(id, request, userDetails.getId());
        return ResponseEntity.ok(gallery);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete gallery", description = "Delete a gallery with option to delete or move photos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gallery deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Gallery not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<String> deleteGallery(
            @Parameter(description = "Gallery ID") @PathVariable Long id,
            @Parameter(description = "Whether to delete photos (true) or move to unorganized (false)") @RequestParam(defaultValue = "false") boolean deletePhotos,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        galleryService.deleteGallery(id, userDetails.getId(), deletePhotos);
        return ResponseEntity.ok("Gallery deleted successfully");
    }
    
    @PostMapping("/move-photos")
    @Operation(summary = "Move photos between galleries", description = "Move selected photos to another gallery or unorganized photos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photos moved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<String> movePhotos(
            @Valid @RequestBody MovePhotosRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        galleryService.movePhotos(request, userDetails.getId());
        return ResponseEntity.ok("Photos moved successfully");
    }
    
    @GetMapping("/dropdown")
    @Operation(summary = "Get galleries for dropdown", description = "Get galleries for use in dropdown menus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Galleries retrieved successfully", content = @Content(schema = @Schema(implementation = GalleryDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<GalleryDto>> getGalleriesForDropdown(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GalleryDto> galleries = galleryService.getGalleriesForDropdown(userDetails.getId());
        return ResponseEntity.ok(galleries);
    }
    

}
