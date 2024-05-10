package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.RegistryEntrySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ForgeRegistryHandler<T> implements PlatformRegistry<T> {

    private final DeferredRegister<T> deferredRegister;

    private final Set<RegistryObjectWrapper<T, ? extends T>> entries = new LinkedHashSet<>();
    private final Set<RegistryObjectWrapper<T, ? extends T>> entriesView = Collections.unmodifiableSet(this.entries);

    public ForgeRegistryHandler(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @Override
    public <I extends T> RegistryEntrySupplier<T, I> register(String name, Supplier<I> sup) {
        RegistryObjectWrapper<T, I> entry = new RegistryObjectWrapper<>(this.deferredRegister.register(name, sup));
        this.entries.add(entry);
        return entry;
    }

    @Override
    public void registerContent(Object eventBus) {
        if (eventBus instanceof IEventBus bus)
            this.deferredRegister.register(bus);
    }

    @Override
    public void registerContent() {
        throw new UnsupportedOperationException("Use the one accepting an object");
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries() {
        return this.entriesView;
    }
}
