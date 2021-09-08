package io.github.amaizeing.oktopus.util;

import java.util.Map;
import java.util.Set;

public class Validation {

    private Validation() {

    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

}
