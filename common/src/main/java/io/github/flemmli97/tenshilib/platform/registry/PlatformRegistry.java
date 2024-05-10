package io.github.flemmli97.tenshilib.platform.registry;

import java.util.Collection;
import java.util.function.Supplier;

public interface PlatformRegistry<T> {

    <I extends T> RegistryEntrySupplier<T, I> register(String name, Supplier<I> sup);

    /**
     * Impl only for neoforge
     *
     * @param r The ModEventbus to pass
     */
    default void registerContent(Object r) {
        this.registerContent();
    }

    void registerContent();

    Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries();
}
