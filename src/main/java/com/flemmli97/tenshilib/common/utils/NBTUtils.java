package com.flemmli97.tenshilib.common.utils;

import net.minecraft.nbt.CompoundNBT;

public class NBTUtils {

    public static <T extends Enum<T>> T get(Class<T> clss, CompoundNBT nbt, String key, T def) {
        try {
            return Enum.valueOf(clss, nbt.getString(key));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}