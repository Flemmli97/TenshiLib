package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryEntrySupplier<T, I extends T> extends Supplier<I> {

    ResourceLocation getID();

    Holder<T> asHolder();
}
