package io.github.flemmli97.tenshilib.loader.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public interface LoaderRegister<T> {

    default <I extends T> RegistryEntrySupplier<T, I> register(String name, Supplier<I> sup) {
        return this.register(name, res -> sup.get());
    }

    <I extends T> RegistryEntrySupplier<T, I> register(String name, Function<ResourceLocation, I> func);

    void addAlias(ResourceLocation from, ResourceLocation to);

    void registerContent();

    Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries();
}
