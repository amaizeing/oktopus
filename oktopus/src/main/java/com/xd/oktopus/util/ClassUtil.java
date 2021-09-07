package com.xd.oktopus.util;

import com.xd.oktopus.exception.ClassLoadException;

public class ClassUtil {

    private ClassUtil() {
    }

    private static String cleanText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("[^\\x00-\\x7F]", "")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("\\p{C}", "").trim();
    }

    private static Class<?> findClass(String classPath) throws ClassNotFoundException {
        var loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtil.class.getClassLoader();
        }
        return Class.forName(cleanText(classPath), true, loader);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(String classPath) {
        try {
            final var clazz = Class.forName(cleanText(classPath));
            final var constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            final var obj = constructor.newInstance();
            return (T) obj;
        } catch (Exception ex) {
            throw new ClassLoadException(ex);
        }
    }

    public static <T> T getInstance(Class<T> clazz) {
        return getInstance(clazz.getName());
    }

}
