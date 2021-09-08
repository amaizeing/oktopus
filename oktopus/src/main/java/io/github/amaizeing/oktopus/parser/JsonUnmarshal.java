package io.github.amaizeing.oktopus.parser;

import io.github.amaizeing.oktopus.util.JsonUtil;

public class JsonUnmarshal implements Unmarshal<Object> {

    @Override
    public Object unmarshal(final byte[] bytes, final Class<Object> clazz) {
        return JsonUtil.toObject(bytes, clazz);
    }

}
