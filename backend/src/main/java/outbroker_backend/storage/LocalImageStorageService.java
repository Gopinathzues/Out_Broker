package outbroker_backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalImageStorageService implements ImageStorageService {

    private final Path uploadRoot;

    public LocalImageStorageService(
            @Value("${app.upload.directory:uploads}") String uploadDirectory) {

        this.uploadRoot = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String store(
            MultipartFile file,
            String directory,
            String fileName
    ) throws IOException {

        Path targetPath = uploadRoot
                .resolve(directory)
                .resolve(fileName)
                .normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IOException("Invalid file path");
        }

        Files.createDirectories(targetPath.getParent());

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath);
        }

        return Paths.get(directory, fileName)
                .toString()
                .replace('\\', '/');
    }

    @Override
    public void delete(String storagePath) throws IOException {

        if (storagePath == null || storagePath.isBlank()) {
            return;
        }

        Path filePath = uploadRoot
                .resolve(storagePath)
                .normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new IOException("Invalid file deletion path");
        }

        Files.deleteIfExists(filePath);
    }
}