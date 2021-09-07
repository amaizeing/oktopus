package com.amaizeing.oktopus.exception;

public class OktopusAnnotationException extends RuntimeException {

    public OktopusAnnotationException() {
    }

    public OktopusAnnotationException(final String message) {
        super(message);
    }

    public OktopusAnnotationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OktopusAnnotationException(final Throwable cause) {
        super(cause);
    }

}
