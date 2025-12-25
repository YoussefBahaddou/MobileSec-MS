package com.mobilesec.reportgen.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirestoreConfig {

    @Value("${firestore.project-id:}")
    private String projectId;

    @Value("${firestore.credentials-path:}")
    private String credentialsPath;

    @Bean
    public Firestore firestore() {
        try {
            FirestoreOptions.Builder builder = FirestoreOptions.newBuilder();
            if (StringUtils.hasText(projectId)) {
                builder.setProjectId(projectId);
            }

            if (StringUtils.hasText(credentialsPath)) {
                try (FileInputStream stream = new FileInputStream(credentialsPath)) {
                    builder.setCredentials(GoogleCredentials.fromStream(stream));
                }
            } else {
                builder.setCredentials(GoogleCredentials.getApplicationDefault());
            }

            return builder.build().getService();
        } catch (IOException e) {
            log.error("Failed to initialize Firestore client", e);
            throw new IllegalStateException("Unable to initialize Firestore client", e);
        }
    }
}
