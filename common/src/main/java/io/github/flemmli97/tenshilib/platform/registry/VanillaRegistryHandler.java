package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Registry;
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
    private final Map<VanillaEntrySupplier<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<VanillaEntrySupplier<T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());

    public VanillaRegistryHandler(ResourceKey<? extends Registry<T>> key, String modid) {
        this.key = key;
        this.modid = modid;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup) {
        ResourceLocation id = new ResourceLocation(this.modid, name);
        VanillaEntrySupplier<I> v = new VanillaEntrySupplier<>(id);
        this.entries.putIfAbsent((VanillaEntrySupplier<T>) v, () -> sup.get());
        return v;
    }

    @Override
    public void finalize(Object r) {
        Registry<T> registry = this.registryFrom(this.key);
        if (registry == null)
            throw new NullPointerException("Registry is null during init " + this.key);
        this.entries.forEach((v, s) -> {
            Registry.register(registry, v.getID(), s.get());
            v.updateValue(registry);
        });
    }

    @SuppressWarnings("unchecked")
    protected <T> Registry<T> registryFrom(ResourceKey<? extends Registry<T>> key) {
        Registry<?> reg = Registry.REGISTRY.get(key.location());
        if (reg == null)
            throw new NullPointerException("Failed to get a corresponding registry for " + key);
        return (Registry<T>) reg;
    }

    @Override
    public Collection<? extends RegistryEntrySupplier<T>> getEntries() {
        return this.entriesView;
    }
}
