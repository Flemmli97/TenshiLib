package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VanillaEntrySupplier<T, I extends T> implements RegistryEntrySupplier<T, I> {

    private final ResourceLocation name;
    @Nullable
    private I value;

    protected VanillaEntrySupplier(ResourceLocation res) {
        this.name = res;
    }

    @SuppressWarnings("unchecked")
    public void updateValue(Registry<T> registry) {
        this.value = (I) registry.get(this.name);
    }

    @Override
    public I get() {
        I ret = this.value;
        Objects.requireNonNull(ret, () -> "Object not present: " + this.name);
        return ret;
    }

    @Override
    public ResourceLocation getID() {
        return this.name;
    }

    @Override
    public Holder<T> asHolder() {
        return Holder.direct(this.value);
    }
}
