package outbroker_backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {

    String store(
            MultipartFile file,
            String directory,
            String fileName
    ) throws IOException;

    void delete(String storagePath) throws IOException;
}