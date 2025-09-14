package io.github.flemmli97.tenshilib.common.registry;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.ai.TargetPosition;
import io.github.flemmli97.tenshilib.common.entity.data.SyncedEntityData;
import io.github.flemmli97.tenshilib.common.utils.StreamCodecs;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class TenshilibSyncableEntityDatas {

    public static final ResourceKey<? extends Registry<SyncedEntityData<?>>> SYNCABLE_ENTITY_DATA_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "syncable_entity_data"));
    public static final LoaderRegistryAccess.CustomLoaderRegistry<SyncedEntityData<?>> SYCABLE_ENTITY_DATAS = LoaderRegistryAccess.INSTANCE.newRegistry(SYNCABLE_ENTITY_DATA_KEY,
            null, true, true);

    public static final RegistryEntrySupplier<SyncedEntityData<?>, SyncedEntityData<Vec3>> VEC_3 = register("vec_3", StreamCodecs.VEC3.cast());
    public static final RegistryEntrySupplier<SyncedEntityData<?>, SyncedEntityData<TargetPosition>> TARGET_POS = register("target_position", StreamCodecs.TARGET_POSITION.cast());

    private static <T> RegistryEntrySupplier<SyncedEntityData<?>, SyncedEntityData<T>> register(String name, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        return SYCABLE_ENTITY_DATAS.register().register(name, () -> new SyncedEntityData<>(codec));
    }
}
