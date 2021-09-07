package com.xd.oktopus;

import com.fasterxml.jackson.core.type.TypeReference;

public interface Deserializer<T> {

    T deserializer(byte[] byes, TypeReference<T> typeReference);

}
