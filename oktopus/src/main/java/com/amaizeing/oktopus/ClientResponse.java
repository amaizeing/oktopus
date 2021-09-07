package com.amaizeing.oktopus;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ClientResponse {


    private int httpStatusCode;
    private byte[] body;
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

    ClientResponse setBody(final byte[] body) {
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

}
