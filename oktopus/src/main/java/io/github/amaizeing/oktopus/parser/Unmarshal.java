package io.github.amaizeing.oktopus.parser;

public interface Unmarshal<T> {

    T unmarshal(byte[] bytes, Class<T> clazz);

}
