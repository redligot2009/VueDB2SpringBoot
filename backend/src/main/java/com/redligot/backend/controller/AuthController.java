package com.redligot.backend.controller;

import com.redligot.backend.model.User;
import com.redligot.backend.payload.JwtAuthenticationResponse;
import com.redligot.backend.payload.LoginRequest;
import com.redligot.backend.payload.SignUpRequest;
import com.redligot.backend.payload.UpdateProfileRequest;
import com.redligot.backend.payload.UserProfile;
import com.redligot.backend.repository.UserRepository;
import com.redligot.backend.security.JwtTokenProvider;
import com.redligot.backend.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Username or email already exists")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("Signup request received - username: {}, email: {}", signUpRequest.getUsername(),
                signUpRequest.getEmail());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        // Creating user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User result = userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved", content = @Content(schema = @Schema(implementation = UserProfile.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // Create UserProfile DTO with profile picture information
        boolean hasProfilePicture = user.getProfilePictureData() != null && user.getProfilePictureData().length > 0;
        UserProfile userProfile = new UserProfile(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            hasProfilePicture,
            user.getProfilePictureFilename(),
            user.getProfilePictureContentType(),
            user.getProfilePictureSize()
        );

        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/profile-picture")
    @Operation(summary = "Get user profile picture", description = "Get the profile picture for the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found or no profile picture")
    })
    public ResponseEntity<?> getProfilePicture(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (user.getProfilePictureData() == null || user.getProfilePictureData().length == 0) {
            return ResponseEntity.status(404).body("No profile picture found");
        }

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(user.getProfilePictureContentType()))
                .body(user.getProfilePictureData());
    }

    @PutMapping("/profile")
    @Operation(
        summary = "Update user profile", 
        description = "Update the current user's profile information and optional profile picture. Form fields (username, email, password) are sent as URL parameters, while the profile picture is sent as multipart form data."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Profile picture file (optional)",
        content = @Content(
            mediaType = "multipart/form-data",
            schema = @Schema(type = "string", format = "binary")
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or username/email already taken"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "New username (3-50 characters)", required = true)
            @RequestParam("username") String username,
            @Parameter(description = "New email address", required = true)
            @RequestParam("email") String email,
            @Parameter(description = "New password (6-100 characters, optional - leave blank to keep current)")
            @RequestParam(value = "password", required = false) String password,
            @Parameter(description = "Profile picture file (optional - JPG, PNG, GIF, WebP, max 5MB)")
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // Check if username is already taken by another user
        if (!user.getUsername().equals(username) &&
                userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // Check if email is already taken by another user
        if (!user.getEmail().equals(email) &&
                userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        // Update user information
        user.setUsername(username);
        user.setEmail(email);

        // Update password only if provided
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        // Debug logging for profile picture
        logger.info("Profile update request - username: {}, email: {}, password provided: {}, profilePicture: {}", 
                   username, email, (password != null && !password.trim().isEmpty()), 
                   (profilePicture != null ? profilePicture.getOriginalFilename() : "null"));
        
        if (profilePicture != null) {
            logger.info("Profile picture details - filename: {}, size: {}, content type: {}", 
                       profilePicture.getOriginalFilename(), profilePicture.getSize(), profilePicture.getContentType());
        }
        
                    // Handle profile picture
            if (profilePicture == null) {
                // Remove profile picture
                user.setProfilePictureData(null);
                user.setProfilePictureFilename(null);
                user.setProfilePictureContentType(null);
                user.setProfilePictureSize(null);
                logger.info("Profile picture removed (null provided)");
            } else if (!profilePicture.isEmpty()) {
                // New profile picture provided
                try {
                    user.setProfilePictureFilename(profilePicture.getOriginalFilename());
                    user.setProfilePictureContentType(profilePicture.getContentType());
                    user.setProfilePictureSize(profilePicture.getSize());
                    user.setProfilePictureData(profilePicture.getBytes());
                    logger.info("Profile picture saved successfully - {} bytes", profilePicture.getSize());
                } catch (Exception e) {
                    logger.error("Error processing profile picture: {}", e.getMessage(), e);
                    return ResponseEntity.badRequest().body("Error processing profile picture: " + e.getMessage());
                }
            } else {
                logger.info("Empty profile picture provided - keeping existing");
            }

        User updatedUser = userRepository.save(user);

        // Build descriptive response message
        StringBuilder responseMessage = new StringBuilder("Profile updated successfully");
        
        if (profilePicture != null && !profilePicture.isEmpty()) {
            responseMessage.append(" with new profile picture: ")
                         .append(profilePicture.getOriginalFilename())
                         .append(" (")
                         .append(profilePicture.getSize())
                         .append(" bytes, ")
                         .append(profilePicture.getContentType())
                         .append(")");
        } else if (profilePicture == null) {
            responseMessage.append(" (profile picture removed)");
        } else {
            responseMessage.append(" (no profile picture changes)");
        }
        
        responseMessage.append(". Updated fields: username, email");
        if (password != null && !password.trim().isEmpty()) {
            responseMessage.append(", password");
        }

        return ResponseEntity.ok(responseMessage.toString());
    }

}
