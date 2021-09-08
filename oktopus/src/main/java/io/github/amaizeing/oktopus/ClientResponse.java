package io.github.amaizeing.oktopus;

import java.util.HashMap;
import java.util.Map;

public class ClientResponse {

    private int httpStatusCode;
    private Object body;
    private Map<String, String> headers;
    private Exception exception;

    void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
    }

    ClientResponse setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    ClientResponse setBody(final Object body) {
        this.body = body;
        return this;
    }

    ClientResponse setHeaders(final Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    ClientResponse setException(final Exception exception) {
        this.exception = exception;
        return this;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBody() {
        return (T) body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Exception getException() {
        return exception;
    }

}
