package io.github.flemmli97.tenshilib.neoforge.loader.registry;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
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
    public void addAlias(ResourceLocation from, ResourceLocation to) {
        this.deferredRegister.addAlias(from, to);
    }

    @Override
    public void registerContent() {
        IEventBus bus = ModList.get().getModContainerById(this.deferredRegister.getNamespace())
                .map(ModContainer::getEventBus).orElse(null);
        if (bus == null) {
            throw new IllegalStateException("Unable to get mod eventbus for modid " + this.deferredRegister.getNamespace());
        }
        this.deferredRegister.register(bus);
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries() {
        return this.entriesView;
    }
}
