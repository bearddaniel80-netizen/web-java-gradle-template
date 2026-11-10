package example.controller;
import example.service.FastapiClient;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class MinioController {
    private final FastapiClient fastapi;

    public MinioController(FastapiClient fastapi) {
        this.fastapi = fastapi;
    }

    private static String segment(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    @PostMapping("/api/uploads")
    public ResponseEntity<Object> upload(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "description", required = false) String description
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        Map<String, Object> fields = description == null
                ? Map.of()
                : Map.of("description", description);

        try {
            return fastapi.uploadMetadata(
                    "/api/uploads",
                    file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename(),
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                    fields
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }
    }
}
