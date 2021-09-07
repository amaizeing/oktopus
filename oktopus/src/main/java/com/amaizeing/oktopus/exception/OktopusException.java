package com.amaizeing.oktopus.exception;

public class OktopusException extends RuntimeException {

    public OktopusException() {
    }

    public OktopusException(final String message) {
        super(message);
    }

    public OktopusException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OktopusException(final Throwable cause) {
        super(cause);
    }

}
