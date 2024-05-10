package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class VanillaRegistryHandler<T> implements PlatformRegistry<T> {

    private final ResourceKey<? extends Registry<T>> key;
    private final String modid;
    private final Map<VanillaEntrySupplier<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<VanillaEntrySupplier<T, ? extends T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());

    public VanillaRegistryHandler(ResourceKey<? extends Registry<T>> key, String modid) {
        this.key = key;
        this.modid = modid;
    }

    @Override
    public <I extends T> RegistryEntrySupplier<T, I> register(String name, Supplier<I> sup) {
        ResourceLocation id = new ResourceLocation(this.modid, name);
        VanillaEntrySupplier<T, I> v = new VanillaEntrySupplier<>(id);
        this.entries.putIfAbsent(v, sup);
        return v;
    }

    @Override
    public void registerContent() {
        Registry<T> registry = this.registryFrom();
        this.entries.forEach((v, s) -> {
            Registry.register(registry, v.getID(), s.get());
            v.updateValue(registry);
        });
    }

    @SuppressWarnings("unchecked")
    protected Registry<T> registryFrom() {
        Registry<?> reg = BuiltInRegistries.REGISTRY.get(this.key.location());
        if (reg == null)
            throw new NullPointerException("Failed to get a corresponding registry for " + this.key);
        return (Registry<T>) reg;
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T, ? extends T>> getEntries() {
        return this.entriesView;
    }
}
