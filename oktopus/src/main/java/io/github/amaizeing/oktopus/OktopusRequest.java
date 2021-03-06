package io.github.amaizeing.oktopus;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.MODULE)
public class OktopusRequest {

    private final Class<?> requestClass;
    private Object[] urlArgs;
    private Object[] headersArgs;
    private Object[] bodyArgs;
    private RequestErrorHandler errorHandler;
//    private Duration timeout;
//    private RetryConfig retryConfig;

    private OktopusRequest(final Class<?> clazz) {
        this.requestClass = clazz;
    }

    public static OktopusRequest on(Class<?> clazz) {
        return new OktopusRequest(clazz);
    }

    public OktopusRequest urlArgs(Object... args) {
        this.urlArgs = args;
        return this;
    }

    public OktopusRequest headersArgs(Object... args) {
        this.headersArgs = args;
        return this;
    }

    public OktopusRequest requestBodyArgs(Object... args) {
        this.bodyArgs = args;
        return this;
    }

    public OktopusRequest onServerError(RequestErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

//    public OktopusRequest retryConfig(RetryConfig config) {
//        this.retryConfig = config;
//        return this;
//    }

//    public OktopusRequest timeout(Duration timeout) {
//        this.timeout = timeout;
//        return this;
//    }

}
