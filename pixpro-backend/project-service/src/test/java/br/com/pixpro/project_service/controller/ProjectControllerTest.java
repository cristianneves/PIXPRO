//package br.com.pixpro.project_service.controller;
//
//import br.com.pixpro.project_service.config.SecurityConfig;
//import br.com.pixpro.project_service.config.JwtAuthenticationFilter;
//import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
//import br.com.pixpro.project_service.dto.ImageMetadataDto;
//import br.com.pixpro.project_service.dto.UpdateProjectRequestDto;
//import br.com.pixpro.project_service.model.ProcessingStatus;
//import br.com.pixpro.project_service.model.Project;
//import br.com.pixpro.project_service.service.ProjectService;
//import br.com.pixpro.project_service.service.JwtService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.test.web.servlet.MockMvc;
//
//import javax.crypto.SecretKey;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//@AutoConfigureMockMvc
//@WebMvcTest(ProjectController.class)
//@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
//class ProjectControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ProjectService projectService;
//
//    @MockBean
//    private JwtService jwtService;
//
//    @MockBean
//    private UserDetailsService userDetailsService;
//
//    @MockBean
//    @org.springframework.beans.factory.annotation.Qualifier("customAuthenticationEntryPoint")
//    private org.springframework.security.web.AuthenticationEntryPoint authenticationEntryPoint;
//
//    @Value("${jwt.secret.key}")
//    private String secretKey;
//
//    private String testToken;
//    private UserDetails testUserDetails;
//
//    @BeforeEach
//    void setUp() {
//        // Create test user details
//        testUserDetails = User.builder()
//                .username("test@example.com")
//                .password("password")
//                .authorities(Collections.emptyList())
//                .build();
//
//        // Generate a valid JWT token
//        testToken = generateTestToken("test@example.com", 123L);
//
//        // Mock JwtService methods to work with the generated token
//        when(jwtService.extractUsername(testToken)).thenReturn("test@example.com");
//        when(jwtService.isTokenValid(eq(testToken), any(org.springframework.security.core.userdetails.UserDetails.class))).thenReturn(true);
//
//        // Mock extractAllClaims to return the user claims
//        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
//        when(claims.get("userId", Long.class)).thenReturn(123L);
//        when(claims.get("roles", List.class)).thenReturn(List.of());
//        when(jwtService.extractAllClaims(testToken)).thenReturn(claims);
//
//        // Mock UserDetailsService
//        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
//    }
//
//    private String generateTestToken(String email, Long userId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", userId);
//
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
//
//        return Jwts.builder()
//                .claims(claims)
//                .subject(email)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
//                .signWith(key)
//                .compact();
//    }
//
//        @Test
//        @DisplayName("Should create project successfully")
//    void createProject_ValidRequest_ReturnsCreated() throws Exception {
//        // Arrange
//        CreateProjectRequestDto requestDto = new CreateProjectRequestDto("Test Project");
//
//        Project createdProject = new Project();
//        createdProject.setId(1L);
//        createdProject.setName("Test Project");
//        createdProject.setUserId(123L);
//
//        when(projectService.createProject(any(CreateProjectRequestDto.class), anyLong()))
//                .thenReturn(createdProject);
//
//        // Act & Assert
//    mockMvc.perform(post("/api/projects")
//            .with(csrf())
//            .header("Authorization", "Bearer " + testToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.name").value("Test Project"))
//                .andExpect(jsonPath("$.userId").value(123));
//
//        verify(projectService).createProject(any(CreateProjectRequestDto.class), eq(123L));
//    }
//
//        @Test
//        @DisplayName("Should get user projects")
//    void getUserProjects_ValidUser_ReturnsProjects() throws Exception {
//        // Arrange
//        Project project1 = new Project();
//        project1.setId(1L);
//        project1.setName("Project 1");
//        project1.setUserId(123L);
//
//        Project project2 = new Project();
//        project2.setId(2L);
//        project2.setName("Project 2");
//        project2.setUserId(123L);
//
//        when(projectService.findProjectsByUserId(123L)).thenReturn(List.of(project1, project2));
//
//        // Act & Assert
//    mockMvc.perform(get("/api/projects")
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].name").value("Project 1"))
//                .andExpect(jsonPath("$[1].name").value("Project 2"));
//
//        verify(projectService).findProjectsByUserId(123L);
//    }
//
//        @Test
//        @DisplayName("Should get project by ID")
//    void getProjectById_ValidId_ReturnsProject() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setName("Test Project");
//        project.setUserId(123L);
//
//        when(projectService.findProjectById(projectId, 123L)).thenReturn(project);
//
//        // Act & Assert
//    mockMvc.perform(get("/api/projects/{id}", projectId)
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.name").value("Test Project"));
//
//        verify(projectService).findProjectById(projectId, 123L);
//    }
//
//        @Test
//        @DisplayName("Should get download URL for image")
//    void getDownloadUrl_ValidImageId_ReturnsUrl() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 1L;
//        String downloadUrl = "https://example.com/download";
//
//        when(projectService.generateDownloadUrlForImage(projectId, imageId, 123L))
//                .thenReturn(downloadUrl);
//
//        // Act & Assert
//    mockMvc.perform(get("/api/projects/{projectId}/images/{imageId}/download-url", projectId, imageId)
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.downloadUrl").value(downloadUrl));
//
//        verify(projectService).generateDownloadUrlForImage(projectId, imageId, 123L);
//    }
//
//        @Test
//        @DisplayName("Should add images to project")
//    void addImagesToProject_ValidFiles_ReturnsMetadata() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", "test data".getBytes());
//
//        ImageMetadataDto metadataDto = new ImageMetadataDto(
//                1L,
//                "test.jpg",
//                ProcessingStatus.UPLOAD_PENDING,
//                projectId,
//                LocalDateTime.now()
//        );
//
//        when(projectService.addImagesToProject(eq(projectId), eq(123L), anyList()))
//                .thenReturn(List.of(metadataDto));
//
//        // Act & Assert
//    mockMvc.perform(multipart("/api/projects/{projectId}/images", projectId)
//            .file(file)
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].fileName").value("test.jpg"));
//
//        verify(projectService).addImagesToProject(eq(projectId), eq(123L), anyList());
//    }
//
//        @Test
//        @DisplayName("Should update project")
//    void updateProject_ValidRequest_ReturnsUpdatedProject() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//        UpdateProjectRequestDto requestDto = new UpdateProjectRequestDto("Updated Name");
//
//        Project updatedProject = new Project();
//        updatedProject.setId(projectId);
//        updatedProject.setName("Updated Name");
//        updatedProject.setUserId(123L);
//
//        when(projectService.updateProject(eq(projectId), eq(123L), any(UpdateProjectRequestDto.class)))
//                .thenReturn(updatedProject);
//
//        // Act & Assert
//    mockMvc.perform(put("/api/projects/{id}", projectId)
//            .with(csrf())
//            .header("Authorization", "Bearer " + testToken)
//            .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Updated Name"));
//
//        verify(projectService).updateProject(eq(projectId), eq(123L), any(UpdateProjectRequestDto.class));
//    }
//
//        @Test
//        @DisplayName("Should delete project")
//    void deleteProject_ValidId_ReturnsNoContent() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//
//        doNothing().when(projectService).deleteProject(projectId, 123L);
//
//        // Act & Assert
//    mockMvc.perform(delete("/api/projects/{id}", projectId)
//            .with(csrf())
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isNoContent());
//
//        verify(projectService).deleteProject(projectId, 123L);
//    }
//
//        @Test
//        @DisplayName("Should delete image from project")
//    void deleteImage_ValidIds_ReturnsNoContent() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 1L;
//
//        doNothing().when(projectService).deleteImageFromProject(projectId, imageId, 123L);
//
//        // Act & Assert
//    mockMvc.perform(delete("/api/projects/{projectId}/images/{imageId}", projectId, imageId)
//            .with(csrf())
//            .header("Authorization", "Bearer " + testToken))
//                .andExpect(status().isNoContent());
//
//        verify(projectService).deleteImageFromProject(projectId, imageId, 123L);
//    }
//
//    @SuppressWarnings("unchecked")
//    private static <T> List<T> anyList() {
//        return any(List.class);
//    }
//}
