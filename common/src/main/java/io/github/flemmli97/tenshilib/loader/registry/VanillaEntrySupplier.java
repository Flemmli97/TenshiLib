package io.github.flemmli97.tenshilib.loader.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class VanillaEntrySupplier<T, I extends T> implements RegistryEntrySupplier<T, I> {

    private final ResourceLocation name;
    private Holder<T> holder;

    protected VanillaEntrySupplier(ResourceLocation res) {
        this.name = res;
    }

    public void updateValue(Registry<T> registry) {
        this.holder = registry.getHolder(this.name).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public I get() {
        Objects.requireNonNull(this.holder, () -> "Holder not present: " + this.name);
        return (I) this.holder.value();
    }

    @Override
    public ResourceLocation getID() {
        return this.name;
    }

    @Override
    public ResourceKey<T> getKey() {
        return this.holder.unwrapKey().get();
    }

    @Override
    public Holder<T> asHolder() {
        return this.holder;
    }
}
