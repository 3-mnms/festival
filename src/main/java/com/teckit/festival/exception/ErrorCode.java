package com.teckit.festival.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 4xx (클라이언트 오류)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),
    NO_AUTHORITY(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 공연만 처리할 수 있습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인한 사용자만 가능합니다."),
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),
    FESTIVAL_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 상세 정보를 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 일정을 찾을 수 없습니다."),
    USER_GEOCODE_FAIL(HttpStatus.NOT_FOUND, "사용자 주소 정보를 확인할 수 없습니다."),
    DUPLICATE_FAVORITE(HttpStatus.CONFLICT, "이미 관심 상품에 등록된 공연입니다."),
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "관심 상품을 찾을 수 없습니다."),
    NEARBY_FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "근처 페스티벌을 찾을 수 없습니다."),

    DUPLICATE_FID(HttpStatus.CONFLICT, "이미 존재하는 공연 식별자(fid)입니다."),
    DELETION_CONFLICT(HttpStatus.CONFLICT, "연관 데이터로 인해 삭제할 수 없습니다."),

    //페스티벌 리뷰
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "기대평을 찾을 수 없습니다."),
    REVIEW_NOT_ALLOWED(HttpStatus.FORBIDDEN, "허용되지 않는 행동입니다. 작성자만이 기대평을 수정 또는 삭제할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT,"공연 한 개당 하나의 기대평만 생성할 수 있습니다."),

    // 5xx (서버/백엔드 연동 오류)
    KAFKA_PUBLISH_FAILED(HttpStatus.BAD_GATEWAY, "이벤트 발행에 실패했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."), // 추가된 부분
    AI_RESPONSE_FAILED(HttpStatus.BAD_GATEWAY, "AI 응답이 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}