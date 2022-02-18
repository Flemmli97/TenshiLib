package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryEntrySupplier<T> extends Supplier<T> {

    ResourceLocation getID();
}
