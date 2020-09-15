package com.flemmli97.tenshilib.api.config;

public interface IConfigArrayValue<T extends IConfigArrayValue<T>> {

    T readFromString(String[] s);

    String[] writeToString();

    String usage();
}
