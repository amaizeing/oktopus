package com.xd.oktopus;

import lombok.Builder;

import java.time.Duration;

@Builder
public class RetryConfig {

    private final int retryTimes;
    private final Duration initialInterval;
    private final float backoffCoefficient;

}
