package com.flemmli97.tenshilib.common.utils;

public interface ObjectConverter<T, M> {

    M convertFrom(T t);
}
