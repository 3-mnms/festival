package com.teckit.festival.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Festival not found"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Schedule not found"),
    NO_AUTHORITY(HttpStatus.FORBIDDEN, "No authority"),
    // 필요하면 추가...

    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}