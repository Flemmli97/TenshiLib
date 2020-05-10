package com.flemmli97.tenshilib.common.javahelper;

public interface StringParser<T> {

    public static final StringParser<Object> objToString = t -> t.toString();

    public String getString(T t);

}
