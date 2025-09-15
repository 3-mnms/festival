package com.teckit.festival.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final String s3BucketName;

    // S3에 파일 업로드 (백엔드 업로드 방식)
    public String uploadFile(MultipartFile file) {
        try {
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

    // S3에서 파일 삭제
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("S3 삭제 실패: " + fileUrl, e);
        }
    }

    public void replaceFile(Supplier<String> oldFileGetter,
                            Consumer<String> newFileSetter,
                            MultipartFile newFile) {
        // 기존 파일 삭제
        String oldFile = oldFileGetter.get();
        if (oldFile != null && !oldFile.isBlank()) {
            deleteFile(oldFile);
        }
        // 새 파일 업로드
        String newUrl = uploadFile(newFile);
        newFileSetter.accept(newUrl);
    }

    public void replaceFiles(List<String> oldFiles,
                             List<MultipartFile> newFiles,
                             Consumer<List<String>> newFilesSetter) {
        // 기존 파일들 삭제
        if (oldFiles != null && !oldFiles.isEmpty()) {
            oldFiles.forEach(this::deleteFile);
        }
        // 새 파일들 업로드
        List<String> newUrls = newFiles.stream()
                .map(this::uploadFile)
                .toList();
        newFilesSetter.accept(newUrls);
    }

    private String extractKeyFromUrl(String fileUrl) throws MalformedURLException {
        URL url = new URL(fileUrl);
        return url.getPath().substring(1); // 앞에 "/" 제거
    }
}
