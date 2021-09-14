package io.github.amaizeing.oktopus;

import java.time.Duration;

public interface CacheService {

    void put(Object key, Object value, Duration ttl);

    <T> T get(Object key);
}
