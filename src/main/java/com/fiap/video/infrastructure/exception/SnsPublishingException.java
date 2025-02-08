package com.fiap.video.infrastructure.exception;

public class SnsPublishingException extends RuntimeException {
    public SnsPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}

