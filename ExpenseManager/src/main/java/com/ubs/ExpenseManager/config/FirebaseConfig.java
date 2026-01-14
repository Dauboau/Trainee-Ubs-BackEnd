package com.ubs.ExpenseManager.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS_BASE64}")
    private String credentials;

    @Value("${FIREBASE_BUCKET}")
    private String bucketName;

    @Bean
    public Bucket storageBucket() throws IOException {

        byte[] decodedBytes = Base64.getDecoder().decode(credentials);

        InputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket(bucketName)
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return StorageClient.getInstance().bucket();
    }
}
