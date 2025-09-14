package io.github.flemmli97.tenshilib.common.entity.data;

import io.github.flemmli97.tenshilib.common.registry.TenshilibSyncableEntityDatas;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SyncedEntityData<T>(StreamCodec<RegistryFriendlyByteBuf, T> serializer) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedEntityData<?>> STREAM_CODEC = ByteBufCodecs.registry(TenshilibSyncableEntityDatas.SYNCABLE_ENTITY_DATA_KEY);

    @Override
    public String toString() {
        return String.format("SyncedEntityData[%s]", TenshilibSyncableEntityDatas.SYCABLE_ENTITY_DATAS.registry().getKey(this));
    }
}
