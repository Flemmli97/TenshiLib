package io.github.flemmli97.tenshilib.api.config;

public interface IConfigValue<T extends IConfigValue<T>> {

    T readFromString(String s);

    String writeToString();
}
