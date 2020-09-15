package com.flemmli97.tenshilib.common.utils;

public interface StringParser<T> {

    StringParser<Object> objToString = t -> t.toString();

    String getString(T t);

}
