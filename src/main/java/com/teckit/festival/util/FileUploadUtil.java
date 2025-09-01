package com.teckit.festival.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class FileUploadUtil {

    private FileUploadUtil() {
        // 인스턴스화 방지를 위한 private 생성자
    }

    // 파일을 S3에 저장하고 저장된 파일의 URL을 반환합니다.
    public static String uploadFile(AmazonS3 s3Client, String bucketName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }
}