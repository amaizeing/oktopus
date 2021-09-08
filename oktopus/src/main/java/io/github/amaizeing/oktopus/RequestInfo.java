package io.github.amaizeing.oktopus;


import io.github.amaizeing.oktopus.annotation.method.HttpMethod;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

@Data
@Accessors(chain = true)
class RequestInfo {

    private Object requestInstance;
    private HttpMethod method;
    private String url;
    private Map<?, String> urls;
    private Object body;
    private Map<String, Object> header;
    private Class<?> responseTypeOnSuccess;
    private Class<?> responseTypeOnFailure;
    private Object cacheKey;
    private Method cacheTtlMethod;
    private RequestErrorHandler errorHandler;
    private Duration timeout;
    private RetryConfig retryConfig;

    RequestInfo() {
    }

}
