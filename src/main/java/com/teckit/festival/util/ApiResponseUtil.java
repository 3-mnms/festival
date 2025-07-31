package com.teckit.festival.util;

import com.teckit.festival.exception.global.SuccessResponse;
import org.springframework.http.ResponseEntity;

public class ApiResponseUtil {
    public static <T> ResponseEntity<SuccessResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new SuccessResponse<>(true, data, message));
    }
}