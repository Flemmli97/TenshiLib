package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VanillaEntrySupplier<T> implements RegistryEntrySupplier<T> {

    private final ResourceLocation name;
    @Nullable
    private T value;

    protected VanillaEntrySupplier(ResourceLocation res) {
        this.name = res;
    }

    public void updateValue(Registry<T> registry) {
        this.value = registry.get(this.name);
    }

    @Override
    public T get() {
        T ret = this.value;
        Objects.requireNonNull(ret, () -> "Object not present: " + this.name);
        return ret;
    }

    @Override
    public ResourceLocation getID() {
        return this.name;
    }
}
