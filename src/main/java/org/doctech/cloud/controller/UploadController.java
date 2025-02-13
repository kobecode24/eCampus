package org.doctech.cloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.doctech.cloud.service.CloudinaryService;
import org.doctech.user.model.User;
import org.doctech.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    public UploadController(CloudinaryService cloudinaryService, UserService userService) {
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId) {
        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            String url = cloudinaryService.uploadFile(tempFile);

            if (!tempFile.delete()) {
                logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }

            // Update the avatar URL in the database
            User user = userService.updateUserAvatar(userId, url);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("avatarUrl", user.getAvatar());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Upload failed"));
        }
    }
}

