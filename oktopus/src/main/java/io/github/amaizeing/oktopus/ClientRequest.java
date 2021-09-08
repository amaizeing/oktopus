package io.github.amaizeing.oktopus;

import io.github.amaizeing.oktopus.annotation.method.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class ClientRequest {

    private String url;
    private HttpMethod method;
    private Map<String, Object> header;
    private Object requestBody;

    ClientRequest setUrl(final String url) {
        this.url = url;
        return this;
    }

    ClientRequest setMethod(final HttpMethod method) {
        this.method = method;
        return this;
    }

    ClientRequest setHeader(final Map<String, Object> header) {
        this.header = header;
        return this;
    }

    ClientRequest setRequestBody(final Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, Object> getHeader() {
        return new HashMap<>(header);
    }

    @SuppressWarnings("unchecked")
    public <T> T getRequestBody() {
        return (T) requestBody;
    }

}
