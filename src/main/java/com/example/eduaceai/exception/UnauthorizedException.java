package com.example.eduaceai.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
