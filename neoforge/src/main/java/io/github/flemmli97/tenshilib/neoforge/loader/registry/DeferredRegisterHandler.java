package io.github.flemmli97.tenshilib.neoforge.loader.registry;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class DeferredRegisterHandler<T> implements LoaderRegister<T> {

    private final DeferredRegister<T> deferredRegister;

    private final Set<RegistryObjectWrapper<T, ? extends T>> entries = new LinkedHashSet<>();
    private final Set<RegistryObjectWrapper<T, ? extends T>> entriesView = Collections.unmodifiableSet(this.entries);

    public DeferredRegisterHandler(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @Override
    public <I extends T> RegistryEntrySupplier<T, I> register(String name, Function<ResourceLocation, I> func) {
        RegistryObjectWrapper<T, I> entry = new RegistryObjectWrapper<>(this.deferredRegister.register(name, func));
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
