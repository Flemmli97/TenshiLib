package io.github.flemmli97.tenshilib.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class VanillaRegistryHandler<T> implements PlatformRegistry<T> {

    private final Registry<T> registry;
    private final String modid;
    private final Map<VanillaEntrySupplier<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<VanillaEntrySupplier<T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());

    protected VanillaRegistryHandler(Registry<T> registry, String modid) {
        this.registry = registry;
        this.modid = modid;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends T> RegistryEntrySupplier<I> register(String name, Supplier<? extends I> sup) {
        ResourceLocation id = new ResourceLocation(this.modid, name);
        VanillaEntrySupplier<I> v = new VanillaEntrySupplier<>(id);
        this.entries.putIfAbsent((VanillaEntrySupplier<T>) v, () -> (I) sup.get());
        return v;
    }

    @Override
    public void finalize(Object r) {
        this.entries.forEach((v, s) -> {
            Registry.register(this.registry, v.getID(), s.get());
            v.updateValue(this.registry);
        });
    }

    @Override
    public Collection<? extends Supplier<T>> getEntries() {
        return this.entriesView;
    }
}
