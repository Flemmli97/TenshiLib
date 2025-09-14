package io.github.flemmli97.tenshilib.common.registry;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleData;
import io.github.flemmli97.tenshilib.common.particle.ParticleHandlerType;
import io.github.flemmli97.tenshilib.common.particle.data.CirclingData;
import io.github.flemmli97.tenshilib.common.particle.data.ColorData;
import io.github.flemmli97.tenshilib.common.particle.data.EntityFollowData;
import io.github.flemmli97.tenshilib.common.particle.data.MotionData;
import io.github.flemmli97.tenshilib.common.particle.data.MoveToData;
import io.github.flemmli97.tenshilib.common.particle.data.ParticleMetaData;
import io.github.flemmli97.tenshilib.common.particle.data.ScaleData;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Requires calling for {@link TenshiLib#registerSyncedRegistry()} before being able to access this.
 * By default the things here are NOT registered
 */
public class TenshilibParticleHandlerTypes {

    public static final ResourceKey<? extends Registry<ParticleHandlerType<?>>> PARTICLE_HANDLER_TYPES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "particle_handler_types"));
    public static final LoaderRegistryAccess.CustomLoaderRegistry<ParticleHandlerType<?>> PARTICLE_HANDLER_TYPES = LoaderRegistryAccess.INSTANCE.newRegistry(PARTICLE_HANDLER_TYPES_KEY,
            null, true, true);

    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<ColorData>> COLOR = register("color", () -> new ParticleHandlerType<>(ColorData.CODEC, ColorData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<EntityFollowData>> ENTITY_FOLLOW = register("entity_follow", () -> new ParticleHandlerType<>(EntityFollowData.CODEC, EntityFollowData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<MotionData>> MOTION = register("motion", () -> new ParticleHandlerType<>(MotionData.CODEC, MotionData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<MoveToData>> MOVE_TO = register("move_to", () -> new ParticleHandlerType<>(MoveToData.CODEC, MoveToData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<ParticleMetaData>> PARTICLE_META = register("particle_meta", () -> new ParticleHandlerType<>(ParticleMetaData.CODEC, ParticleMetaData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<ScaleData>> SCALE = register("scale", () -> new ParticleHandlerType<>(ScaleData.CODEC, ScaleData.STREAM_CODEC));
    public static final RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<CirclingData>> CIRLING = register("cirling", () -> new ParticleHandlerType<>(CirclingData.CODEC, CirclingData.STREAM_CODEC));

    private static <T extends AdvancedParticleData> RegistryEntrySupplier<ParticleHandlerType<?>, ParticleHandlerType<T>> register(String name, Supplier<ParticleHandlerType<T>> sup) {
        return PARTICLE_HANDLER_TYPES.register().register(name, sup);
    }
}
