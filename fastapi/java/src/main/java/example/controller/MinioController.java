package example.controller;

import example.dto.UploadUrlRequest;
import example.security.Secrets;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class MinioController {
    private final MinioClient minio;
    private final Secrets secrets;
    private final String bucket;

    public MinioController(MinioClient minio, Secrets secrets, @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.secrets = secrets;
        this.bucket = bucket;
    }

    @PostMapping("/api/uploads")
    public Map<String, String> createUploadUrl(@RequestBody UploadUrlRequest file,
            @RequestHeader(value = "x-internal-key", required = false) String key) {
        secrets.verifyInternalKey(key);

        String name = file.getFilename();
        String ext = (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.')) : "";
        String objectName = UUID.randomUUID() + ext;

        return Map.of("object_name", objectName, "url", presign(Method.PUT, objectName, 1, TimeUnit.DAYS));
    }

    @GetMapping("/api/downloads/{*objectName}")
    public Map<String, String> createDownloadUrl(@PathVariable String objectName) {
        String name = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        return Map.of("url", presign(Method.GET, name, 10, TimeUnit.MINUTES));
    }

    private String presign(Method method, String object, int amount, TimeUnit unit) {
        try {
            return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method).bucket(bucket).object(object).expiry(amount, unit).build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}