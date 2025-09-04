package com.teckit.festival.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class FileUploadUtil {

    //단일 파일 저장 (폴더 지정 가능)
    public static String saveFile(MultipartFile file, String uploadDir, String baseUrl, String subDir) {
        if (file == null || file.isEmpty()) return null;
        try {
            File dir = new File(System.getProperty("user.dir") + "/" + uploadDir + "/" + subDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            return baseUrl + "/" + uploadDir + "/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    //여러 파일 저장 (폴더 지정 가능)
    public static List<String> saveFiles(List<MultipartFile> files, String uploadDir, String baseUrl, String subDir) {
        List<String> paths = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                String path = saveFile(f, uploadDir, baseUrl, subDir);
                if (path != null) paths.add(path);
            }
        }
        return paths;
    }
}