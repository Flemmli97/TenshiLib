package io.github.flemmli97.tenshilib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TenshiLib {

    public static final String MODID = "tenshilib";
    public static final Logger logger = LogManager.getLogger("TenshiLib");

    public static boolean isFateLoaded;
    public static boolean isRunecraftoryLoaded;

    public static final TagKey<EntityType<?>> MULTIPART_ENTITY = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), new ResourceLocation("c", "multipart_entity"));
}
