package com.example.eduaceai.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message, String errorCode) {
        super(message, errorCode);
    }
}
