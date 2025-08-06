package com.teckit.festival.exception.global;

import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Swagger 관련 요청은 이 핸들러에서 무시
    private boolean isSwaggerRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui");
    }

    /**
     * 비즈니스 로직에서 발생한 커스텀 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode.name())
                .errorMessage(errorCode.getMessage())
                .build();
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * @Valid 검증 실패 (DTO 바인딩 오류 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .errorMessage(message)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 잘못된 타입 바인딩 (예: Long 필드에 문자 전달)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorResponse response = new ErrorResponse(false, "TYPE_MISMATCH", "잘못된 타입의 요청입니다.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 모든 예외의 fallback 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception e, HttpServletRequest request) {
        e.printStackTrace(); // 🔍 로그로 남겨서 디버깅
        ErrorResponse response = new ErrorResponse(false, "INTERNAL_SERVER_ERROR", "알 수 없는 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}