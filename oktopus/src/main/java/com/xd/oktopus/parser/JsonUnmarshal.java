package com.xd.oktopus.parser;

import com.xd.oktopus.util.JsonUtil;

public class JsonUnmarshal implements Unmarshal<Object> {

    @Override
    public Object unmarshal(final byte[] bytes, final Class<Object> clazz) {
        return JsonUtil.toObject(bytes, clazz);
    }

}
