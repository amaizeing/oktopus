package com.amaizeing.oktopus;

import lombok.Builder;

import java.time.Duration;

@Builder
class RetryConfig {

    private final int retryTimes;
    private final Duration initialInterval;
    private final float backoffCoefficient;

}
