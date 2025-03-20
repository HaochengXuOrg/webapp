package com.example.health_check.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.health_check.model.FileMetadata;
import com.example.health_check.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final AmazonS3 s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${s3.bucket-name}")
    private String bucketName;

    public FileService(AmazonS3 s3Client, FileMetadataRepository fileMetadataRepository) {
        this.s3Client = s3Client;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public FileMetadata uploadFile(MultipartFile file) throws IOException {
        String uniqueKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        s3Client.putObject(new PutObjectRequest(
                bucketName,
                uniqueKey,
                file.getInputStream(),
                null
        ));

        FileMetadata metadata = new FileMetadata(
                uniqueKey,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        return fileMetadataRepository.save(metadata);
    }

    public Optional<FileMetadata> getFileMetadata(Long id) {
        return fileMetadataRepository.findById(id);
    }

    public boolean deleteFile(Long id) {
        Optional<FileMetadata> fileOpt = fileMetadataRepository.findById(id);
        if (fileOpt.isEmpty()) {
            return false;
        }

        FileMetadata fileData = fileOpt.get();

        s3Client.deleteObject(bucketName, fileData.getS3Key());

        fileMetadataRepository.delete(fileData);

        return true;
    }
}
