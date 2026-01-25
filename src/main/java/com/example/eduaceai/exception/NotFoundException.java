package com.example.eduaceai.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends BusinessException {
    public NotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
