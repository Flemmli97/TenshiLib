package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.RegistryEntrySupplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.function.Supplier;

public record ForgeRegistryHandler<T extends IForgeRegistryEntry<T>>(
        DeferredRegister<T> deferredRegister) implements PlatformRegistry<T> {

    @Override
    public <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup) {
        return new RegistryObjectWrapper<>(this.deferredRegister.register(name, sup));
    }

    @Override
    public void finalize(Object r) {
        if (r instanceof IEventBus bus)
            this.deferredRegister.register(bus);
    }

    @Override
    public Collection<? extends Supplier<T>> getEntries() {
        return this.deferredRegister.getEntries();
    }
}
