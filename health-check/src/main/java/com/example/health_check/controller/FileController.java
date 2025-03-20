package com.example.health_check.controller;
import java.util.Map;
import java.util.HashMap;

import com.example.health_check.model.FileMetadata;
import com.example.health_check.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            FileMetadata savedMetadata = fileService.uploadFile(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMetadata);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getFileMetadata(@PathVariable Long id) {
        return fileService.getFileMetadata(id)
                .map(metadata -> {
                    Map<String, String> response = new HashMap<>();
                    String s3Url = "https://s3." + System.getenv("AWS_REGION") + ".amazonaws.com/" + System.getenv("S3_BUCKET") + "/" + metadata.getS3Key();
                    response.put("filePath", s3Url);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        boolean deleted = fileService.deleteFile(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
