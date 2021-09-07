package com.amaizeing.oktopus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class RequestCache {

    private static final RequestCache INSTANCE = new RequestCache();

    private final Map<Object, CacheResult> cache = new HashMap<>();

    private RequestCache() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object key) {
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

    public static void put(Object key, Object value, Object ttl, TimeUnit unit) {
        final var result = new CacheResult(value, Long.parseLong(ttl.toString()), unit);
        INSTANCE.cache.put(key, result);
    }

    private static final class CacheResult {

        private final Object value;
        private final long endTime;

        public CacheResult(final Object value, final long ttl, final TimeUnit unit) {
            this.value = value;
            final var ttlInNano = unit.toNanos(ttl);
            final var ttlInMs = TimeUnit.NANOSECONDS.toMillis(ttlInNano);
            this.endTime = System.currentTimeMillis() + ttlInMs;
        }

        private Object get() {
            if (System.currentTimeMillis() - endTime >= 0) {
                return null;
            }
            return value;
        }

    }

}
