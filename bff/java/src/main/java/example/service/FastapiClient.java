package example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class FastapiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FastapiClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${fastapi.url}") String fastapiUrl,
            @Value("${fastapi.secret-file:/run/secrets/fastapi_internal_key}") String secretFile
    ) {
        if (!StringUtils.hasText(fastapiUrl)) {
            throw new IllegalStateException("FASTAPI_URL must be configured");
        }

        String internalKey;
        try {
            internalKey = Files.readString(Path.of(secretFile), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read FastAPI internal secret: " + secretFile, e);
        }

        if (!StringUtils.hasText(internalKey)) {
            throw new IllegalStateException("FastAPI internal secret is empty: " + secretFile);
        }

        this.objectMapper = objectMapper;
        this.restClient = builder
                .baseUrl(fastapiUrl.replaceAll("/+$", ""))
                .defaultHeader("X-Internal-Key", internalKey)
                .defaultHeader(HttpHeaders.ACCEPT,
                        MediaType.ALL_VALUE)
                .build();
    }

    // ------------------------------------------------------------------
    // Typed JSON (DTO)
    // ------------------------------------------------------------------

    public <T> ResponseEntity<T> getJson(String path, Class<T> type) {
        return getJson(path, Map.of(), type);
    }

    public <T> ResponseEntity<T> getJson(String path, Map<String, String> headers, Class<T> type) {
        return typed(HttpMethod.GET, path, null, headers, MediaType.APPLICATION_JSON,
                spec -> spec.toEntity(type));
    }

    public <T> ResponseEntity<T> getJson(String path, ParameterizedTypeReference<T> type) {
        return getJson(path, Map.of(), type);
    }

    public <T> ResponseEntity<T> getJson(String path, Map<String, String> headers,
                                         ParameterizedTypeReference<T> type) {
        return typed(HttpMethod.GET, path, null, headers, MediaType.APPLICATION_JSON,
                spec -> spec.toEntity(type));
    }

    public <T> ResponseEntity<T> postJson(String path, Object body, Class<T> type) {
        return postJson(path, body, Map.of(), type);
    }

    public <T> ResponseEntity<T> postJson(String path, Object body, Map<String, String> headers,
                                          Class<T> type) {
        return typed(HttpMethod.POST, path, body, headers, MediaType.APPLICATION_JSON,
                spec -> spec.toEntity(type));
    }

    public <T> ResponseEntity<T> postJson(String path, Object body,
                                          ParameterizedTypeReference<T> type) {
        return postJson(path, body, Map.of(), type);
    }

    public <T> ResponseEntity<T> postJson(String path, Object body, Map<String, String> headers,
                                          ParameterizedTypeReference<T> type) {
        return typed(HttpMethod.POST, path, body, headers, MediaType.APPLICATION_JSON,
                spec -> spec.toEntity(type));
    }

    // ------------------------------------------------------------------
    // HTML (raw String)
    // ------------------------------------------------------------------

    public ResponseEntity<String> getHtml(String path) {
        return getHtml(path, Map.of());
    }

    public ResponseEntity<String> getHtml(String path, Map<String, String> headers) {
        return typed(HttpMethod.GET, path, null, headers, MediaType.TEXT_HTML,
                spec -> spec.toEntity(String.class));
    }

    public ResponseEntity<String> postHtml(String path, Object body, Map<String, String> headers) {
        return typed(HttpMethod.POST, path, body, headers, MediaType.TEXT_HTML,
                spec -> spec.toEntity(String.class));
    }

    // ------------------------------------------------------------------
    // Untyped passthrough (JSON -> Object, anything else -> String).
    // Kept for callers that just proxy FastAPI responses.
    // ------------------------------------------------------------------

    public ResponseEntity<Object> get(String path) {
        return get(path, null, Map.of());
    }

    public ResponseEntity<Object> get(String path, Object body, Map<String, String> headers) {
        return execute(HttpMethod.GET, path, body, headers);
    }

    public ResponseEntity<Object> post(String path, Object body, Map<String, String> headers) {
        return execute(HttpMethod.POST, path, body, headers);
    }

    /**
     * Sends filename/content-type/fields as JSON.
     * It intentionally does not forward the uploaded file bytes.
     */
    public ResponseEntity<Object> uploadMetadata(String path, String filename,
                                                 String contentType, Map<String, Object> fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("filename", filename);
        payload.put("content_type", contentType);
        if (fields != null) {
            payload.putAll(fields);
        }
        return post(path, payload, Map.of());
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /**
     * Shared request pipeline for typed calls. Error statuses do not throw;
     * callers inspect the returned status code. Note that if the error body
     * does not match the requested type, deserialization can still fail.
     */
    private <T> ResponseEntity<T> typed(HttpMethod method, String path, Object body,
                                        Map<String, String> headers, MediaType accept,
                                        Function<RestClient.ResponseSpec, ResponseEntity<T>> extractor) {
        RestClient.ResponseSpec spec = buildRequest(method, path, body, headers, accept)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> { /* don't throw */ });
        return extractor.apply(spec);
    }

    private RestClient.RequestBodySpec buildRequest(HttpMethod method, String path, Object body,
                                                    Map<String, String> headers, MediaType accept) {
        RestClient.RequestBodySpec request = restClient.method(method).uri(path);
        if (accept != null) {
            request.accept(accept);
        }
        if (headers != null) {
            headers.forEach(request::header);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON);
            request.body(body);
        }
        return request;
    }

    private ResponseEntity<Object> execute(HttpMethod method, String path,
                                           Object body, Map<String, String> headers) {
        return buildRequest(method, path, body, headers, null)
                .exchange((clientRequest, clientResponse) -> {
                    HttpStatusCode status = clientResponse.getStatusCode();
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    byte[] bytes = clientResponse.getBody().readAllBytes();

                    Object data;
                    if (isJson(contentType)) {
                        if (bytes.length == 0) {
                            data = null;
                        } else {
                            try {
                                data = objectMapper.readValue(bytes, Object.class);
                            } catch (Exception parseError) {
                                data = new String(bytes, StandardCharsets.UTF_8);
                            }
                        }
                    } else {
                        data = new String(bytes, StandardCharsets.UTF_8);
                    }

                    return ResponseEntity.status(status).body(data);
                });
    }

    private static boolean isJson(MediaType contentType) {
        return contentType != null
                && (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                    || contentType.getSubtype().endsWith("+json"));
    }
}
