package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {
    
    private final Path rootLocation = Paths.get("uploads");

    public ImageStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            log.error("Could not create upload directory", e);
        }
    }

    public String saveImage(MultipartFile file, String prefix) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("빈 파일입니다.");
            }

            String filename = prefix + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            file.transferTo(destinationFile);
            
            return destinationFile.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
} 