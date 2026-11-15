package example.controller;

import example.security.Secrets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DefaultController {
    private final Secrets secrets;

    public DefaultController(Secrets secrets) {
        this.secrets = secrets;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/api/data")
    public Map<String, String> data(@RequestHeader(value = "x-internal-key", required = false) String key) {
        secrets.verifyInternalKey(key);
        return Map.of("message", "Hello from Spring");
    }

}