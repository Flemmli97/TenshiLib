package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.resources.ResourceLocation;

/**
 * Simple structure to get stuff from registries
 */
public interface SimpleRegistryWrapper<T> {

    T getFromId(ResourceLocation id);

    ResourceLocation getIDFrom(T entry);

    Iterable<T> getIterator();
}