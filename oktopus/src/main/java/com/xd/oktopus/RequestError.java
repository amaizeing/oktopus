package com.xd.oktopus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestError {

    private Class<?> request;
    private OktopusRequest oktopusRequest;
    private Exception exception;

}
