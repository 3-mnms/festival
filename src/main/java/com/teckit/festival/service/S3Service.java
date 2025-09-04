package com.teckit.festival.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final String s3BucketName;

    /**
     * S3에 파일 업로드 (백엔드 업로드 방식)
     * @param file MultipartFile
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 파일명 중복 방지용 UUID prefix
            String key = "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return "https://" + s3BucketName + ".s3.amazonaws.com/" + key;
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }
}
