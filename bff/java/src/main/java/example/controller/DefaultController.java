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
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class DefaultController {
    private final FastapiClient fastapi;

    public DefaultController(FastapiClient fastapi) {
        this.fastapi = fastapi;
    }

    // Static routes
    @GetMapping("/api/data")
    public ResponseEntity<Object> data() {
        return fastapi.get("/api/data");
    }

}
