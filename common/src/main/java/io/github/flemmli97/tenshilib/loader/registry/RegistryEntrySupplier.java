package io.github.flemmli97.tenshilib.loader.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryEntrySupplier<T, I extends T> extends Supplier<I> {

    ResourceLocation getID();

    ResourceKey<T> getKey();

    Holder<T> asHolder();
}
