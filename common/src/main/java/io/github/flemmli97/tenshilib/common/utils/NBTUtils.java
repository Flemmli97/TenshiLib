package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.nbt.CompoundTag;

public class NBTUtils {

    public static <T extends Enum<T>> T get(Class<T> clss, CompoundTag nbt, String key, T def) {
        try {
            return Enum.valueOf(clss, nbt.getString(key));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}