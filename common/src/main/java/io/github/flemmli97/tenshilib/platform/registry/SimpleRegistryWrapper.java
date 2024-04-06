package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

/**
 * Simple structure to get stuff from registries
 */
public interface SimpleRegistryWrapper<T> {

    T getFromId(ResourceLocation id);

    Optional<T> getOptionalFromId(ResourceLocation id);

    ResourceLocation getIDFrom(T entry);

    Iterable<T> getIterator();

    Collection<T> values();

    boolean contains(ResourceLocation id);

    Collection<ResourceLocation> keys();
}
