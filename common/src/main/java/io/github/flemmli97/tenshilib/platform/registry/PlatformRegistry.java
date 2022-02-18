package io.github.flemmli97.tenshilib.platform.registry;

import java.util.Collection;
import java.util.function.Supplier;

public interface PlatformRegistry<T> {

    <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup);

    /**
     * Register the registry. For fabric passing null is fine. For forge pass in the modbus
     */
    void finalize(Object r);

    Collection<? extends Supplier<T>> getEntries();
}
