package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.platform.registry.RegistryEntrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public record RegistryObjectWrapper<T>(
        RegistryObject<T> object) implements RegistryEntrySupplier<T> {

    @Override
    public ResourceLocation getID() {
        return this.object.getId();
    }

    @Override
    public T get() {
        return this.object.get();
    }

    public RegistryObject<T> getObject() {
        return this.object;
    }
}
