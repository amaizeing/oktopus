package com.xd.oktopus.exception;

public class ClientRequestException extends RuntimeException {

    public ClientRequestException() {
    }

    public ClientRequestException(final String message) {
        super(message);
    }

    public ClientRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ClientRequestException(final Throwable cause) {
        super(cause);
    }

}
