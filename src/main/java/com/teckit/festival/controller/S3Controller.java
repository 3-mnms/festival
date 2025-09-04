/*ackage com.teckit.festival.controller;

import com.teckit.festival.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URL;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload-url")
    public ResponseEntity<String> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType) {

        // Optionally, add logic here to sanitize file names or
        // add a unique identifier (e.g., UUID) to prevent overwrites.
        String objectKey = "uploads/" + fileName;

        String presignedUrl = s3Service.generatePresignedUrl(objectKey, contentType);

        return ResponseEntity.ok(presignedUrl.toString());
    }
}*/