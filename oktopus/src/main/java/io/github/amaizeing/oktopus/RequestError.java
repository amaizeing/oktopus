package io.github.amaizeing.oktopus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.MODULE)
public class RequestError {

    private Class<?> request;
    private OktopusRequest oktopusRequest;
    private Exception exception;

}
