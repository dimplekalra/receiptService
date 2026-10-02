package com.maven.receipts.storage;

import com.maven.receipts.config.StorageProperties;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {
    private final Path uploadDirectory;

    public FileSystemStorageService(StorageProperties properties)
            throws IOException {

        this.uploadDirectory = Paths.get(properties.getUploadDirectory());

        Files.createDirectories(uploadDirectory);
    }

    @Override
    public String store(MultipartFile file) {

        String extension = StringUtils.getFilenameExtension(
                file.getOriginalFilename());

        String filename = UUID.randomUUID() +
                (extension == null ? "" : "." + extension);

        try {

            Files.copy(
                    file.getInputStream(),
                    uploadDirectory.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);

            return filename;

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "Unable to store uploaded file",
                    e);

        }

    }

    @Override
    public Resource load(String storedFilename) {

        return new PathResource(
                uploadDirectory.resolve(storedFilename));

    }

    @Override
    public void delete(String storedFilename) {

        try {

            Files.deleteIfExists(
                    uploadDirectory.resolve(storedFilename));

        } catch (IOException e) {

            throw new UncheckedIOException(
                    e);

        }

    }
}
