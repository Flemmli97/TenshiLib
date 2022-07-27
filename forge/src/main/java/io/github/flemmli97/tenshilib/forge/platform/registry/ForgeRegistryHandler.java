package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.RegistryEntrySupplier;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ForgeRegistryHandler<T> implements PlatformRegistry<T> {

    private final DeferredRegister<T> deferredRegister;

    private final Set<RegistryObjectWrapper<T>> entries = new LinkedHashSet<>();
    private final Set<RegistryObjectWrapper<T>> entriesView = Collections.unmodifiableSet(this.entries);

    public ForgeRegistryHandler(DeferredRegister<T> deferredRegister) {
        this.deferredRegister = deferredRegister;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup) {
        RegistryObjectWrapper<I> entry = new RegistryObjectWrapper<>(this.deferredRegister.register(name, sup));
        this.entries.add((RegistryObjectWrapper<T>) entry);
        return entry;
    }

    @Override
    public void registerContent() {
        this.deferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T>> getEntries() {
        return this.entriesView;
    }
}
