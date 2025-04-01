package com.example.health_check.repository;

import com.example.health_check.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    // Optional<FileMetadata> findByS3Key(String s3Key);
}
