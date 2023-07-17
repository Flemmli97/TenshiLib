package io.github.flemmli97.tenshilib.platform.registry;

import java.util.Collection;
import java.util.function.Supplier;

public interface PlatformRegistry<T> {

    <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup);

    void registerContent();

    Collection<? extends RegistryEntrySupplier<T>> getEntries();
}
