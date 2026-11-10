package example.config;

import example.security.Secrets;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean
    MinioClient minioClient(@Value("${minio.endpoint}") String endpoint) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(Secrets.read("minio_app_user"), Secrets.read("minio_app_password"))
                .build();
    }
}