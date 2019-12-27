package com.flemmli97.tenshilib.common.javahelper;

public interface ObjectConverter<T, M> {

    public M convertFrom(T t);
}
