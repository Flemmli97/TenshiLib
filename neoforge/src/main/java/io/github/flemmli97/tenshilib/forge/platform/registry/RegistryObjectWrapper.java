package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.platform.registry.RegistryEntrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public record RegistryObjectWrapper<T, I extends T>(
        DeferredHolder<T, I> object) implements RegistryEntrySupplier<T, I> {

    @Override
    public ResourceLocation getID() {
        return this.object.getId();
    }

    @Override
    public Holder<T> asHolder() {
        return this.object.getDelegate();
    }

    @Override
    public I get() {
        return this.object.get();
    }

    public DeferredHolder<T, I> getObject() {
        return this.object;
    }
}
