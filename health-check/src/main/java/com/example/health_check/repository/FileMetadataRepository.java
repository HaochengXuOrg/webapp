package com.example.health_check.repository;

import com.example.health_check.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    // 此处也可自定义额外查询方法，比如根据 s3Key 查找
    // Optional<FileMetadata> findByS3Key(String s3Key);
}
