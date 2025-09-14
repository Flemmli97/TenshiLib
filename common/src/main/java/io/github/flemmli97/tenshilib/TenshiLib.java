package io.github.flemmli97.tenshilib;

import io.github.flemmli97.tenshilib.common.registry.TenshilibMemoryModules;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import io.github.flemmli97.tenshilib.common.registry.TenshilibSyncableEntityDatas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TenshiLib {

    public static final String MODID = "tenshilib";
    public static final Logger LOGGER = LogManager.getLogger("TenshiLib");

    public static final TagKey<EntityType<?>> MULTIPART_ENTITY = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath("c", "multipart_entity"));

    private static boolean registered;

    public static void registerRegistry() {
        TenshilibMemoryModules.MODULES.registerContent();
    }

    public static boolean clientRequired() {
        return registered;
    }

    /**
     * Registers registry content that are synced.
     * Calling this makes this mod required on the client
     * <p>
     * The contents could be moved to a separate non server side mod but whatever...
     */
    public static void registerSyncedRegistry() {
        if (registered)
            return;
        registered = true;
        TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES.register().registerContent();
        TenshilibSyncableEntityDatas.SYCABLE_ENTITY_DATAS.register().registerContent();
    }
}
