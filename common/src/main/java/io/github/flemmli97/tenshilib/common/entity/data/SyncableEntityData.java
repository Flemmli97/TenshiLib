package io.github.flemmli97.tenshilib.common.entity.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SyncableEntityData {

    private static final Map<ResourceLocation, SyncedEntityData<?>> REGISTRY = new HashMap<>();

    public static synchronized <T> SyncedEntityData<T> register(ResourceLocation id, StreamCodec<RegistryFriendlyByteBuf, T> serializer) {
        return register(new SyncedEntityData<>(id, serializer));
    }

    private static synchronized <T> SyncedEntityData<T> register(SyncedEntityData<T> inst) {
        if (REGISTRY.putIfAbsent(inst.id, inst) != null)
            throw new IllegalStateException("ID is already registered");
        return inst;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> SyncedEntityData<T> get(ResourceLocation id) {
        return (SyncedEntityData<T>) REGISTRY.get(id);
    }

    public static class SyncedEntityData<T> {

        private final ResourceLocation id;
        private final StreamCodec<RegistryFriendlyByteBuf, T> serializer;

        private SyncedEntityData(ResourceLocation id, StreamCodec<RegistryFriendlyByteBuf, T> serializer) {
            this.id = id;
            this.serializer = serializer;
        }

        public ResourceLocation id() {
            return id;
        }

        public StreamCodec<RegistryFriendlyByteBuf, T> serializer() {
            return serializer;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SyncedEntityData<?> that = (SyncedEntityData<?>) o;
            return Objects.equals(id, that.id) && Objects.equals(serializer, that.serializer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, serializer);
        }

        @Override
        public String toString() {
            return String.format("SyncedEntityData{%s}", id);
        }
    }
}
