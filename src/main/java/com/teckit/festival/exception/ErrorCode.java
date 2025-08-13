package com.teckit.festival.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 4xx (클라이언트 오류)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),                           // ** 추가
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),               // ** 추가
    NO_AUTHORITY(HttpStatus.FORBIDDEN, "권한이 없습니다."),                                // ** 메시지 한글화
    NOT_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 공연만 처리할 수 있습니다."),            // ** 추가

    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),                  // ** 메시지 한글화
    FESTIVAL_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 상세 정보를 찾을 수 없습니다."), // ** 추가
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 일정을 찾을 수 없습니다."),             // ** 메시지 한글화

    DUPLICATE_FID(HttpStatus.CONFLICT, "이미 존재하는 공연 식별자(fid)입니다."),          // ** 추가
    DELETION_CONFLICT(HttpStatus.CONFLICT, "연관 데이터로 인해 삭제할 수 없습니다."),       // ** 추가

    // 5xx (서버/백엔드 연동 오류)
    KAFKA_PUBLISH_FAILED(HttpStatus.BAD_GATEWAY, "이벤트 발행에 실패했습니다."),           // ** 추가
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다."),// ** 추가
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");   // ** 추가

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}