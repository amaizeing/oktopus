package com.xd.oktopus;

import com.xd.oktopus.annotation.method.HttpMethod;
import lombok.Getter;

import java.util.Map;

@Getter
public class ClientRequest {

    private String url;
    private HttpMethod method;
    private Map<String, String> header;
    private Object requestBody;

    ClientRequest setUrl(final String url) {
        this.url = url;
        return this;
    }

    ClientRequest setMethod(final HttpMethod method) {
        this.method = method;
        return this;
    }

    ClientRequest setHeader(final Map<String, String> header) {
        this.header = header;
        return this;
    }

    ClientRequest setRequestBody(final Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

}
