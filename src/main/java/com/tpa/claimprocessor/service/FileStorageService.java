package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.domain.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    StoredFileMetaData storeFile(String claimId, DocumentType documentType, MultipartFile file);

    Path getClaimStoragePath(String claimId);

    record StoredFileMetaData(
            String originalFilename,
            String storedFilename,
            String filePath,
            String contentType,
            long fileSize,
            String checksumSha256
    ) {}
}
