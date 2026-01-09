package com.ubs.ExpenseManager.gateways;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageGateway {
    /**
     * Uploads an image file to the specified path and returns its URL.
     * @param file The image file to upload.
     * @param path The storage path.
     * @return The URL of the uploaded image.
     */
    String uploadImage(MultipartFile file, String path);
}
