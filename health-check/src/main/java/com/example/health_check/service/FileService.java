package com.example.health_check.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.health_check.model.FileMetadata;
import com.example.health_check.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final AmazonS3 s3Client;
    private final FileMetadataRepository fileMetadataRepository;
    private final StatsDClient statsd;
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${s3.bucket-name}")
    private String bucketName;

    public FileService(AmazonS3 s3Client, FileMetadataRepository fileMetadataRepository, StatsDClient statsd) {
        this.s3Client = s3Client;
        this.fileMetadataRepository = fileMetadataRepository;
        this.statsd = statsd;
    }

    public FileMetadata uploadFile(MultipartFile file) throws IOException {

        long startDb = System.currentTimeMillis();

        // First generate UUID for S3 key and DB primary key
        String id = UUID.randomUUID().toString();
        String s3Key = id + "/" + file.getOriginalFilename();

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setContentType(file.getContentType());
        metadata.setSizeBytes(file.getSize());
        metadata.setUploadTime(java.time.LocalDateTime.now());
        metadata.setS3Key(s3Key);

        FileMetadata saved = fileMetadataRepository.save(metadata);

        long endDb = System.currentTimeMillis();
        statsd.recordExecutionTime("db.queryTime", endDb - startDb);

        long startS3 = System.currentTimeMillis();
        s3Client.putObject(new PutObjectRequest(
                bucketName,
                s3Key,
                file.getInputStream(),
                null
        ));
        long endS3 = System.currentTimeMillis();
        statsd.recordExecutionTime("s3.callTime", endS3 - startS3);

        logger.info("File uploaded and metadata saved. ID: {}, S3Key: {}", saved.getId(), s3Key);

        return saved;
    }

    public Optional<FileMetadata> getFileMetadata(String id) {
        long startTime = System.currentTimeMillis();
        logger.info("Looking up file metadata for ID: {}", id);

        Optional<FileMetadata> result = fileMetadataRepository.findById(id);

        long endTime = System.currentTimeMillis();
        statsd.recordExecutionTime("db.queryTime", endTime - startTime);

        if (result.isPresent()) {
            logger.info("Metadata found for ID: {}", id);
        } else {
            logger.warn("No metadata found for ID: {}", id);
        }

        return result;
    }

    public boolean deleteFile(String id) {

        long dbStart = System.currentTimeMillis();
        logger.info("Attempting to delete file with ID: {}", id);

        Optional<FileMetadata> fileOpt = fileMetadataRepository.findById(id);
        long dbEnd = System.currentTimeMillis();
        statsd.recordExecutionTime("db.queryTime", dbEnd - dbStart);

        if (fileOpt.isEmpty()) {
            logger.warn("File with ID {} not found. Deletion aborted.", id);
            return false;
        }

        FileMetadata fileData = fileOpt.get();

        long s3Start = System.currentTimeMillis();
        s3Client.deleteObject(bucketName, fileData.getS3Key());
        long s3End = System.currentTimeMillis();
        statsd.recordExecutionTime("s3.callTime", s3End - s3Start);
        logger.info("S3 object deleted for key: {}", fileData.getS3Key());

        long dbStartDel = System.currentTimeMillis();
        fileMetadataRepository.delete(fileData);
        long dbEndDel = System.currentTimeMillis();
        statsd.recordExecutionTime("db.queryTime", dbEndDel - dbStartDel);
        logger.info("DB record deleted for ID: {}", id);

        return true;
    }
}
