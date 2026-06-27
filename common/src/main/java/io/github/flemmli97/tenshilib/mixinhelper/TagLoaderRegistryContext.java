package io.github.flemmli97.tenshilib.mixinhelper;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

public interface TagLoaderRegistryContext<T> {

    void tenshilib$setRegistry(ResourceKey<? extends Registry<T>> key, RegistryAccess registryAccess);
}
