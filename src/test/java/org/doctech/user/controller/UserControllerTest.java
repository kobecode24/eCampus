package org.doctech.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.doctech.security.JwtTokenProvider;
import org.doctech.security.model.SecurityUser;
import org.doctech.user.dto.*;
import org.doctech.user.model.UserRole;
import org.doctech.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private UserDTO testUserDTO;
    private UUID userId;
    private Authentication authentication;
    private SecurityUser securityUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Setup test user data
        testUserDTO = new UserDTO();
        testUserDTO.setId(userId);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setRoles(Set.of("STUDENT"));
        testUserDTO.setEnabled(true);
        testUserDTO.setPoints(100);
        testUserDTO.setLevel(1);

        // Setup security mocks
        securityUser = mock(SecurityUser.class);
        when(securityUser.getId()).thenReturn(userId);
        when(securityUser.getUsername()).thenReturn("testuser");

        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityUser);

        // Fix for type inference issue
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_STUDENT"));

        doReturn(authorities).when(authentication).getAuthorities();

        // Mock common service calls to prevent null pointer exceptions
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testUserDTO)));
        when(userService.updateUser(any(UUID.class), any(UserDTO.class))).thenReturn(testUserDTO);
        when(userService.isCurrentUser(any(UUID.class), any())).thenReturn(true);
    }

    @Test
    void getUserStatistics_AsAdmin_ShouldReturnStatistics() throws Exception {
        // Given
        UserStatisticsDTO statistics = UserStatisticsDTO.builder()
                .totalActiveUsers(100)
                .newUsersToday(10)
                .totalStudents(80)
                .totalInstructors(20)
                .avgEngagement(25.5)
                .build();

        when(userService.getUserStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalActiveUsers").value(100))
                .andExpect(jsonPath("$.data.totalStudents").value(80));
    }

    @Test
    void getUserStatistics_AsNonAdmin_ShouldBeForbidden() throws Exception {
        // Simulate access denied
        when(userService.getUserStatistics())
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"));

        mockMvc.perform(get("/users/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_WithValidData_ShouldRegisterUser() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123@");
        registerRequest.setRole(UserRole.STUDENT);

        when(userService.registerUser(
                eq("new@example.com"),
                eq("newuser"),
                eq("Password123@"),
                eq(UserRole.STUDENT)
        )).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void register_WithInvalidData_ShouldReturnValidationErrors() throws Exception {
        // Given - create a controller method to handle validation errors
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setUsername("a"); // Too short
        invalidRequest.setPassword("weak");

        // When & Then
        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("test.jwt.token");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("test.refresh.token");

        // When & Then
        mockMvc.perform(post("/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test.refresh.token"));

        verify(userService).updateLastLogin(any(UUID.class));
    }

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnCurrentUser() throws Exception {
        // Given
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        // When & Then - IMPORTANT: Add the principal to the request
        mockMvc.perform(get("/users/me")
                        .principal(authentication))  // This is the key change
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Current user retrieved successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void updateCurrentUser_WithValidData_ShouldUpdateUser() throws Exception {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");

        when(userService.updateUser(any(UUID.class), any(UserDTO.class))).thenReturn(testUserDTO);

        // When & Then - IMPORTANT: Add the principal to the request
        mockMvc.perform(put("/users/me")
                        .principal(authentication)  // This is the key change
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
        // Given
        List<UserDTO> users = List.of(testUserDTO);
        Page<UserDTO> userPage = new PageImpl<>(users);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void getAllUsers_AsNonAdmin_ShouldBeForbidden() throws Exception {
        // Simulate access denied for non-admin
        when(userService.getAllUsers(any(Pageable.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        // Given
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "valid.refresh.token");

        when(tokenProvider.refreshToken("valid.refresh.token")).thenReturn("new.jwt.token");
        when(tokenProvider.refreshRefreshToken("valid.refresh.token")).thenReturn("new.refresh.token");

        // When & Then
        mockMvc.perform(post("/users/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "invalid.refresh.token");

        when(tokenProvider.refreshToken("invalid.refresh.token")).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        mockMvc.perform(post("/users/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void updateUserStatus_AsAdmin_ShouldUpdateStatus() throws Exception {
        // Given
        UserStatusUpdateDTO statusUpdate = new UserStatusUpdateDTO();
        statusUpdate.setEnabled(false);

        testUserDTO.setEnabled(false);
        when(userService.updateUserStatus(eq(userId), eq(false))).thenReturn(testUserDTO);

        // When & Then - Add principal if needed for this admin endpoint
        mockMvc.perform(patch("/users/{id}/status", userId)
                        .principal(authentication) // Add if your controller method needs authentication
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User status updated successfully"));
    }

    @Test
    void updateUserRoles_AsAdmin_ShouldUpdateRoles() throws Exception {
        // Given
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", List.of("ADMIN", "INSTRUCTOR"));

        when(userService.updateUserRoles(eq(userId), any())).thenReturn(testUserDTO);

        // When & Then - Add principal if needed
        mockMvc.perform(put("/users/{id}/roles", userId)
                        .principal(authentication) // Add if your controller method needs authentication
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User roles updated successfully"));
    }

    @Test
    void updateUserRoles_WithEmptyRoles_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, List<String>> request = new HashMap<>();
        request.put("roles", List.of());

        // When & Then - Add principal if needed
        mockMvc.perform(put("/users/{id}/roles", userId)
                        .principal(authentication) // Add if your controller method needs authentication
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Roles list cannot be empty"));
    }

    @Test
    void addPoints_AsAdmin_ShouldAddPoints() throws Exception {
        // Given
        when(userService.addPoints(eq(userId), eq(50))).thenReturn(testUserDTO);

        // When & Then - Add principal if needed
        mockMvc.perform(post("/users/{id}/points/add", userId)
                        .principal(authentication) // Add if your controller method needs authentication
                        .with(csrf())
                        .param("points", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Points added successfully"));
    }

    @Test
    void spendPoints_AsCurrentUser_ShouldSpendPoints() throws Exception {
        // Given
        when(userService.isCurrentUser(eq(userId), any())).thenReturn(true);
        when(userService.spendPoints(eq(userId), eq(20))).thenReturn(testUserDTO);

        // When & Then - Add principal if needed
        mockMvc.perform(post("/users/{id}/points/spend", userId)
                        .principal(authentication) // Add if your controller method needs authentication
                        .with(csrf())
                        .param("points", "20")
                        .param("type", "BADGE_PURCHASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Points spent successfully"));
    }

    @Test
    void getUserById_AsCurrentUser_ShouldReturnUser() throws Exception {
        // Given
        when(userService.isCurrentUser(eq(userId), any())).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(testUserDTO);

        // When & Then - Add principal if needed
        mockMvc.perform(get("/users/{id}", userId)
                        .principal(authentication)) // Add if your controller method needs authentication
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void getAuthDebug_ShouldReturnDebugInfo() throws Exception {
        // Given
        when(securityUser.getUsername()).thenReturn("testuser");

        // When & Then - for auth debug, principal is essential
        mockMvc.perform(get("/users/auth/debug")
                        .principal(authentication)) // This is critical for this endpoint
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication debug info"))
                .andExpect(jsonPath("$.data.authenticated").value(true));
    }
}