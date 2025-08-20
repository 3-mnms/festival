package com.teckit.festival.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class FileUploadService {

    /**
     * 파일을 저장하고 저장된 파일의 URL을 반환합니다.
     * 실제로는 클라우드 스토리지(AWS S3 등)에 업로드하는 로직이 들어갑니다.
     * @param file 업로드할 MultipartFile 객체
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 예시 URL을 반환합니다.
        String fileName = file.getOriginalFilename();
        return "https://example.com/uploads/" + fileName;
    }
}