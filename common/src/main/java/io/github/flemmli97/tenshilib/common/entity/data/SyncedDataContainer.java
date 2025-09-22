package io.github.flemmli97.tenshilib.common.entity.data;

import com.google.common.collect.ImmutableMap;
import io.github.flemmli97.tenshilib.common.network.S2CSyncedMobData;
import io.github.flemmli97.tenshilib.common.utils.TypedResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Vanillas is not very extensible if you need additional data types
 */
public class SyncedDataContainer<T extends Entity & SyncedMobDataHandler> {

    private final T entity;
    private final Map<TypedResource<?>, SyncedContainer<?>> map;
    private boolean isDirty;

    private SyncedDataContainer(T entity, Map<TypedResource<?>, SyncedContainer<?>> map) {
        this.entity = entity;
        this.map = map;
    }

    public static <T extends Entity & SyncedMobDataHandler> Builder<T> builder(T mob) {
        return new Builder<>(mob);
    }

    @SuppressWarnings("unchecked")
    public <D> SyncedContainer<D> getContainer(TypedResource<D> id) {
        return (SyncedContainer<D>) this.map.get(id);
    }

    public <D> D get(TypedResource<D> id) {
        return this.getContainer(id).value();
    }

    public <D> void set(TypedResource<D> id, D value) {
        this.set(id, value, true);
    }

    /**
     * @param syncImmediate If false will send it similarly to vanilla when needed
     */
    public <D> void set(TypedResource<D> id, D value, boolean syncImmediate) {
        SyncedContainer<D> container = this.getContainer(id);
        if (!Objects.equals(value, container.value)) {
            container.value = value;
            if (!syncImmediate) {
                container.dirty = true;
                this.isDirty = true;
            } else {
                S2CSyncedMobData.send(this.entity, List.of(container));
            }
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void sendDirtyEntriesToTracking() {
        List<SyncedDataContainer.SyncedContainer<?>> list = new ArrayList<>();
        for (SyncedContainer<?> container : this.map.values()) {
            if (container.dirty) {
                list.add(container);
                container.dirty = false;
            }
        }
        this.isDirty = false;
        S2CSyncedMobData.send(this.entity, list);
    }

    public void sendEntriesTo(ServerPlayer player) {
        S2CSyncedMobData.sendTo(this.entity, List.copyOf(this.map.values()), player);
    }

    public void update(List<SyncedContainer<?>> values) {
        for (SyncedContainer<?> container : values) {
            SyncedContainer<?> get = this.getContainer(container.id());
            if (get != null) {
                this.assign(get, container);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D> void assign(SyncedContainer<?> source, SyncedContainer<D> target) {
        if (!Objects.equals(source.syncedEntityData, target.syncedEntityData)) {
            throw new IllegalStateException(String.format("Invalid entity data for entity %s - [source: %s, target: %s]", this.entity, source, target));
        } else {
            SyncedContainer<D> current = (SyncedContainer<D>) source;
            current.setValue(target.value);
        }
    }

    public static class Builder<T extends Entity & SyncedMobDataHandler> {

        private final T entity;
        private final Map<TypedResource<?>, SyncedContainer<?>> map = new HashMap<>();

        public Builder(T entity) {
            this.entity = entity;
        }

        public <D> Builder<T> define(TypedResource<D> id, SyncedEntityData<D> type, D initialValue) {
            this.map.put(id, new SyncedContainer<>(id, type, initialValue));
            return this;
        }

        public SyncedDataContainer<T> build() {
            return new SyncedDataContainer<>(this.entity, ImmutableMap.copyOf(this.map));
        }
    }

    public static class SyncedContainer<T> {

        private final TypedResource<T> id;
        private final SyncedEntityData<T> syncedEntityData;
        private T value;
        private boolean dirty;

        public SyncedContainer(TypedResource<T> id, SyncedEntityData<T> syncedEntityData, T initialValue) {
            this.id = id;
            this.syncedEntityData = syncedEntityData;
            this.value = initialValue;
        }

        @SuppressWarnings("unchecked")
        public static <T> SyncedContainer<T> from(RegistryFriendlyByteBuf buf) {
            TypedResource<T> id = (TypedResource<T>) TypedResource.STREAM_CODEC.decode(buf);
            SyncedEntityData<T> data = (SyncedEntityData<T>) SyncedEntityData.STREAM_CODEC.decode(buf);
            boolean none = buf.readBoolean();
            return new SyncedContainer<>(id, data, none ? data.serializer().decode(buf) : null);
        }

        public void write(RegistryFriendlyByteBuf buf) {
            TypedResource.STREAM_CODEC.encode(buf, this.id);
            SyncedEntityData.STREAM_CODEC.encode(buf, this.syncedEntityData);
            buf.writeBoolean(this.value != null);
            if (this.value != null)
                this.syncedEntityData.serializer().encode(buf, this.value);
        }

        private void setValue(T value) {
            this.value = value;
        }

        public TypedResource<T> id() {
            return this.id;
        }

        public T value() {
            return this.value;
        }
    }
}
