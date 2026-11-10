package example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlRequest {
    @JsonProperty("content_type")
    private String contentType;
    private String filename;
}