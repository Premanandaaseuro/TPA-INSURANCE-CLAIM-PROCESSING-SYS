package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.exception.FileStorageException;
import com.tpa.claimprocessor.exception.InvalidDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootStoragePath;

    public FileStorageServiceImpl(@Value("${app.storage.location:storage/claims}") String storageLocation) {
        this.rootStoragePath = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStoragePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize root storage directory: " + storageLocation, e);
        }
    }

    @Override
    public StoredFileMetaData storeFile(String claimId, DocumentType documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File cannot be empty or null for " + documentType);
        }

        String rawFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");

        // Path Traversal Security Check
        if (rawFilename.contains("..") || rawFilename.contains("/") || rawFilename.contains("\\")) {
            throw new InvalidDocumentException("Filename contains invalid path sequence: " + rawFilename);
        }

        // Generate safe stored filename
        String extension = "";
        int extIndex = rawFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = rawFilename.substring(extIndex);
        }
        String prefix = documentType == DocumentType.CLAIM_FORM ? "claim_form" : "combined_document";
        String storedFilename = prefix + extension;

        Path claimDir = getClaimStoragePath(claimId);

        try {
            Files.createDirectories(claimDir);
            Path targetLocation = claimDir.resolve(storedFilename).normalize();

            // Verify final target path stays strictly inside claimDir (prevent directory traversal)
            if (!targetLocation.startsWith(claimDir)) {
                throw new FileStorageException("Cannot store file outside target claim directory");
            }

            // Save file and compute SHA-256 hash simultaneously
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = file.getInputStream();
                 DigestInputStream dis = new DigestInputStream(inputStream, md)) {
                Files.copy(dis, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String checksum = HexFormat.of().formatHex(md.digest());

            return new StoredFileMetaData(
                    rawFilename,
                    storedFilename,
                    targetLocation.toString(),
                    file.getContentType(),
                    file.getSize(),
                    checksum
            );

        } catch (NoSuchAlgorithmException e) {
            throw new FileStorageException("SHA-256 algorithm not available", e);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file for claim " + claimId + ": " + rawFilename, e);
        }
    }

    @Override
    public Path getClaimStoragePath(String claimId) {
        // Sanitize claimId to ensure no path traversal in claimId parameter
        String sanitizedClaimId = claimId.replaceAll("[^a-zA-Z0-9\\-]", "");
        return rootStoragePath.resolve(sanitizedClaimId).normalize();
    }
}
