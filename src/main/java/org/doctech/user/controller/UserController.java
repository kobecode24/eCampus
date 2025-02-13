package org.doctech.user.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.dto.PagedResponse;
import org.doctech.points.model.TransactionType;
import org.doctech.security.model.SecurityUser;
import org.doctech.user.dto.LoginRequest;
import org.doctech.user.dto.PasswordUpdateRequest;
import org.doctech.user.dto.RegisterRequest;
import org.doctech.user.dto.UserDTO;
import org.doctech.user.model.User;
import org.doctech.user.service.UserService;
import org.doctech.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.registerUser(
                request.getEmail(),
                request.getUsername(),
                request.getPassword(),
                request.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User registered successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");

        return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
    }

//    // Authentication Endpoints
//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        UserDTO user = userService.registerUser(
//                registerRequest.getEmail(),
//                registerRequest.getUsername(),
//                registerRequest.getPassword(),
//                registerRequest.getRole()
//        );
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new ApiResponse(true, "User registered successfully", user));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUsername(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        String jwt = tokenProvider.generateToken(authentication);
//
//        return ResponseEntity.ok(new ApiResponse(true, "User logged in successfully", jwt));
//    }

    // User Profile Management
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserDTO userDTO = userService.getUserById(securityUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Current user retrieved successfully", userDTO));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> updateCurrentUser(
            @Valid @RequestBody UserDTO userDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        UserDTO updatedUser = userService.updateUser(securityUser.getId(), userDTO);
        return ResponseEntity.ok(new ApiResponse(true, "User updated successfully", updatedUser));
    }

    // Admin User Management
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        PagedResponse<UserDTO> response = PagedResponse.of(users.getContent(), users);
        return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, principal)")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse(true, "User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(new ApiResponse(true, "User updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully", null));
    }

    // User Points Management
    @PostMapping("/{id}/points/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addPoints(
            @PathVariable UUID id,
            @RequestParam Integer points,
            @RequestParam(required = false) String description) {
        UserDTO updatedUser = userService.addPoints(id, points);
        return ResponseEntity.ok(new ApiResponse(true, "Points added successfully", updatedUser));
    }

    @PostMapping("/{id}/points/spend")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, principal)")
    public ResponseEntity<ApiResponse> spendPoints(
            @PathVariable UUID id,
            @RequestParam Integer points,
            @RequestParam TransactionType type,
            @RequestParam(required = false) String description) {
        UserDTO updatedUser = userService.spendPoints(id, points);
        return ResponseEntity.ok(new ApiResponse(true, "Points spent successfully", updatedUser));
    }

    // User Badge Management
    @GetMapping("/{id}/badges")
    public ResponseEntity<ApiResponse> getUserBadges(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse(true, "User badges retrieved successfully",
                userService.getUserBadges(id)));
    }

    @PostMapping("/{id}/badges/{badgeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> awardBadge(
            @PathVariable UUID id,
            @PathVariable UUID badgeId) {
        UserDTO updatedUser = userService.awardBadge(id, badgeId);
        return ResponseEntity.ok(new ApiResponse(true, "Badge awarded successfully", updatedUser));
    }

    // User Statistics
    @GetMapping("/stats/top")
    public ResponseEntity<ApiResponse> getTopUsers(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(new ApiResponse(true, "Top users retrieved successfully",
                userService.getTopUsersByPoints(limit)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        String refreshToken = refreshRequest.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Refresh token is required", null));
        }

        try {
            String newToken = tokenProvider.refreshToken(refreshToken);
            Map<String, String> tokens = new HashMap<>();
            tokens.put("token", newToken);

            return ResponseEntity.ok(new ApiResponse(true, "Token refreshed successfully", tokens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid refresh token", null));
        }
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        userService.updatePassword(securityUser.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true, "Password updated successfully", null));
    }
}