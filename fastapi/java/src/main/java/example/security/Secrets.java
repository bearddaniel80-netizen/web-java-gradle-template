package example.security;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Component
public class Secrets {
    private final String internalKey = read("fastapi_internal_key");

    /** /run/secrets/<name> first, then env var (local dev). */
    public static String read(String name) {
        Path path = Path.of("/run/secrets", name);
        try {
            if (Files.exists(path)) return Files.readString(path).strip();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read secret " + name, e);
        }
        String value = System.getenv(name);
        if (value == null) throw new IllegalStateException("Missing secret " + name);
        return value;
    }

    public void verifyInternalKey(String provided) {
        boolean ok = provided != null && MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                internalKey.getBytes(StandardCharsets.UTF_8));
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
    }
}