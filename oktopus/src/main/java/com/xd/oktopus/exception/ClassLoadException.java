package com.xd.oktopus.exception;

public class ClassLoadException extends RuntimeException {

    public ClassLoadException() {
    }

    public ClassLoadException(final String message) {
        super(message);
    }

    public ClassLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ClassLoadException(final Throwable cause) {
        super(cause);
    }

}
