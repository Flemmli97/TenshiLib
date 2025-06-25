package io.github.flemmli97.tenshilib.fabric.loader.registry;

import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class VanillaEntrySupplier<T, I extends T> implements RegistryEntrySupplier<T, I> {

    private final ResourceKey<T> key;
    private Holder<T> holder;

    protected VanillaEntrySupplier(ResourceKey<T> res) {
        this.key = res;
    }

    public void updateValue(Registry<T> registry) {
        this.holder = registry.getHolder(this.key).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public I get() {
        Objects.requireNonNull(this.holder, () -> "Holder not present: " + this.key);
        return (I) this.holder.value();
    }

    @Override
    public ResourceLocation getID() {
        return this.key.location();
    }

    @Override
    public ResourceKey<T> getKey() {
        return this.key;
    }

    @Override
    public Holder<T> asHolder() {
        Objects.requireNonNull(this.holder, () -> "Holder not present: " + this.key);
        return this.holder;
    }
}
