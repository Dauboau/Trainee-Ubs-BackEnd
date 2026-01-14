package com.ubs.ExpenseManager.gateways.adapters;

import com.google.cloud.storage.Bucket;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.JpegWriter;

import com.ubs.ExpenseManager.exception.ApiException;
import com.ubs.ExpenseManager.gateways.ImageStorageGateway;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FirebaseStorageAdapter implements ImageStorageGateway {

    private final Bucket storageBucket;

    @Value("${FIREBASE_BUCKET_ENV}")
    public String bucketEnvironment;

    /**
     * Uploads a file to Firebase Storage and returns its public URL.
     * @param file The file to upload.
     * @param fileName The desired file name in Firebase Storage.
     * @return The public URL of the uploaded file.
     */
    @Override
    public String uploadImage(MultipartFile file, String fileName) {
        try {
            fileName = String.format("%s/%s", bucketEnvironment, fileName);
            storageBucket.create(fileName, clearMetadataFirebase(file), file.getContentType());
            return String.format("https://storage.googleapis.com/%s/%s", storageBucket.getName(), fileName);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.FAILED_DEPENDENCY, "Failed to upload file to Firebase Storage");
        }
    }

    private byte[] clearMetadataFirebase(MultipartFile file) throws IOException {
        ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());
        return image.bytes(JpegWriter.Default);
    }

}
