package com.flemmli97.tenshilib.api.config;

public interface IConfigValue<T extends IConfigValue<T>> {

    public T readFromString(String s);

    public String writeToString();

    public String usage();
}
