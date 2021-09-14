package io.github.amaizeing.oktopus;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class DefaultCacheService implements CacheService {

    private static final DefaultCacheService INSTANCE = new DefaultCacheService();
    private final Map<Object, CacheResult> cache = new HashMap<>();
    private static final float TTL_FACTOR = 0.9F;

    DefaultCacheService() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        var result = INSTANCE.cache.get(key);
        if (result == null) {
            return null;
        }
        var value = result.get();
        if (value != null) {
            return (T) value;
        }
        INSTANCE.cache.remove(key);
        return null;
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        final var result = new CacheResult(value, ttl);
        INSTANCE.cache.put(key, result);
    }

    private static final class CacheResult {

        private final Object value;
        private final long endTime;

        public CacheResult(final Object value, Duration ttl) {
            this.value = value;
            this.endTime = System.currentTimeMillis() + (long) (ttl.toMillis() * DefaultCacheService.TTL_FACTOR);
        }

        private Object get() {
            if (System.currentTimeMillis() - endTime >= 0) {
                return null;
            }
            return value;
        }

    }

}
