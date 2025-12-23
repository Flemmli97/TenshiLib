package io.github.flemmli97.tenshilib.fabric.loader.registry;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class VanillaRegisterHandler<T> implements LoaderRegister<T> {

    private final ResourceKey<? extends Registry<T>> key;
    private final String modid;
    private final Map<VanillaEntrySupplier<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<VanillaEntrySupplier<T, ? extends T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());
    private final Map<ResourceLocation, ResourceLocation> alias = new HashMap<>();

    public VanillaRegisterHandler(ResourceKey<? extends Registry<T>> key, String modid) {
        this.key = key;
        this.modid = modid;
    }

    @Override
    public <I extends T> RegistryEntrySupplier<T, I> register(String name, Function<ResourceLocation, I> func) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modid, name);
        VanillaEntrySupplier<T, I> v = new VanillaEntrySupplier<>(ResourceKey.create(this.key, id));
        this.entries.putIfAbsent(v, () -> func.apply(id));
        return v;
    }

    @Override
    public void addAlias(ResourceLocation from, ResourceLocation to) {
        this.alias.put(from, to);
    }

    @Override
    public void registerContent() {
        DeferredRegistrationHandler.add(this.key.location(), this);
    }

    @SuppressWarnings("unchecked")
    protected Registry<T> registryFrom() {
        Registry<?> reg = BuiltInRegistries.REGISTRY.get(this.key.location());
        if (reg == null)
            throw new NullPointerException("Failed to get a corresponding register for " + this.key);
        return (Registry<T>) reg;
    }

    void finalizeRegister() {
        Registry<T> registry = this.registryFrom();
        this.entries.forEach((v, s) -> {
            Registry.register(registry, v.getID(), s.get());
            v.bind(registry);
        });
        this.alias.forEach(registry::addAlias);
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries() {
        return this.entriesView;
    }

    @Override
    public String toString() {
        return String.format("Registration handler for %s for %s", this.key, this.modid);
    }
}
