package com.flemmli97.tenshilib.api.config;

import net.minecraftforge.common.config.Configuration;

public interface IConfigSerializable<T extends IConfigSerializable<T>> {

    /**
     * Read and set value from config
     */
    public T config(Configuration config, String configCategory);
}
