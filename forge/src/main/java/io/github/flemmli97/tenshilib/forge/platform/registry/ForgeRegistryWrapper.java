package io.github.flemmli97.tenshilib.forge.platform.registry;

import com.mojang.serialization.Codec;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.Optional;

public record ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>>(
        IForgeRegistry<T> registry) implements SimpleRegistryWrapper<T> {

    @Override
    public T getFromId(ResourceLocation id) {
        return this.registry.getValue(id);
    }

    @Override
    public Optional<T> getOptionalFromId(ResourceLocation id) {
        return this.registry.containsKey(id) ? Optional.ofNullable(this.registry.getValue(id)) : Optional.empty();
    }

    @Override
    public ResourceLocation getIDFrom(T entry) {
        return entry.getRegistryName();
    }

    @Override
    public Iterable<T> getIterator() {
        return this.registry;
    }

    @Override
    public Collection<T> values() {
        return this.registry.getValues();
    }

    @Override
    public boolean contains(ResourceLocation id) {
        return this.registry.containsKey(id);
    }

    @Override
    public Collection<ResourceLocation> keys() {
        return this.registry.getKeys();
    }

    @Override
    public Codec<T> byNameCodec() {
        return this.registry.getCodec();
    }
}
