package com.flemmli97.tenshilib.api.config;

import java.util.List;

public interface IConfigListValue<T extends IConfigListValue<T>> {

    T readFromString(List<String> s);

    List<String> writeToString();
}
