package com.example.health_check.controller;
import java.util.Map;
import java.util.HashMap;

import com.example.health_check.model.FileMetadata;
import com.example.health_check.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.timgroup.statsd.StatsDClient;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final StatsDClient statsd;
    private final FileService fileService;

    public FileController(FileService fileService, StatsDClient statsd) {
        this.fileService = fileService;
        this.statsd = statsd;
    }

    @PostMapping
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file) {

        long startTime = System.currentTimeMillis();
        statsd.incrementCounter("api.files.hitCount");
        logger.info("POST /files - attempting to upload file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            logger.warn("POST /files - file is empty, returning HTTP 400");
            return ResponseEntity.badRequest().build();
        }
        try {
            long s3Start = System.currentTimeMillis();
            FileMetadata savedMetadata = fileService.uploadFile(file);
            long s3End = System.currentTimeMillis();
            statsd.recordExecutionTime("s3.callTime", s3End - s3Start);
            long endTime = System.currentTimeMillis();
            statsd.recordExecutionTime("api.files.latency", endTime - startTime);
            logger.info("File upload success, metadata ID: {}", savedMetadata.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMetadata);
        } catch (IOException e) {
            logger.error("IOException while uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getFileMetadata(@PathVariable String id) {

        long startTime = System.currentTimeMillis();
        statsd.incrementCounter("api.files.metadata.hitCount");
        logger.info("GET /files/{} - retrieving file metadata", id);

        return fileService.getFileMetadata(id)
                .map(metadata -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("id", metadata.getId());
                    response.put("file_name", metadata.getFileName());
                    String uploadDate = metadata.getUploadTime().toLocalDate().toString();
                    response.put("upload_date", uploadDate);

                    String s3Url = "https://s3." + System.getenv("AWS_REGION")
                            + ".amazonaws.com/" + System.getenv("S3_BUCKET")
                            + "/" + metadata.getS3Key();
                    response.put("url", s3Url);

                    logger.info("Metadata found for id={}, returning JSON", id);
                    long endTime = System.currentTimeMillis();
                    statsd.recordExecutionTime("api.files.metadata.latency", endTime - startTime);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Metadata not found for id={}", id);
                    long endTime = System.currentTimeMillis();
                    statsd.recordExecutionTime("api.files.metadata.latency", endTime - startTime);

                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {

        long startTime = System.currentTimeMillis();
        statsd.incrementCounter("api.files.delete.hitCount");
        logger.info("DELETE /files/{} - attempting deletion", id);

        boolean deleted = fileService.deleteFile(id);

        if (deleted) {
            logger.info("File with id={} deleted successfully.", id);

            long endTime = System.currentTimeMillis();
            statsd.recordExecutionTime("api.files.delete.latency", endTime - startTime);

            return ResponseEntity.noContent().build(); // 204
        } else {
            logger.warn("File with id={} not found, cannot delete.", id);
            long endTime = System.currentTimeMillis();
            statsd.recordExecutionTime("api.files.delete.latency", endTime - startTime);

            return ResponseEntity.notFound().build();
        }
    }
}
