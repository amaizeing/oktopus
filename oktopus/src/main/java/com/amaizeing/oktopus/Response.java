package com.amaizeing.oktopus;

import java.util.Map;

public class Response<T> {

    private int httpStatusCode;
    private T data;
    private Map<String, String> headers;
    private Exception exception;

    Response<T> setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    Response<T> setData(final T data) {
        this.data = data;
        return this;
    }

    Response<T> setHeaders(final Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    Response<T> setException(final Exception exception) {
        this.exception = exception;
        return this;
    }

    int getHttpStatusCode() {
        return httpStatusCode;
    }

    T getData() {
        return data;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    public Exception getException() {
        return exception;
    }

}
