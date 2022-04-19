package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.stream.Collectors;

public record VanillaRegistryWrapper<T>(Registry<T> delegate) implements SimpleRegistryWrapper<T> {

    @Override
    public T getFromId(ResourceLocation id) {
        return this.delegate.get(id);
    }

    @Override
    public ResourceLocation getIDFrom(T entry) {
        return this.delegate.getKey(entry);
    }

    @Override
    public Iterable<T> getIterator() {
        return this.delegate;
    }

    @Override
    public Collection<T> values() {
        return this.delegate.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean contains(ResourceLocation id) {
        return this.delegate.containsKey(id);
    }
}
