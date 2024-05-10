package io.github.flemmli97.tenshilib.platform;

import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class PlatformUtils {

    public static final PlatformUtils INSTANCE = InitUtil.getPlatformInstance(PlatformUtils.class,
            "io.github.flemmli97.tenshilib.fabric.platform.PlatformUtilsImpl",
            "io.github.flemmli97.tenshilib.forge.platform.PlatformUtilsImpl");

    /**
     * Creates a registry handler for the matching key.
     * The registry needs to exist for the key else an exeption will be thrown.
     */
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new VanillaRegistryHandler<>(key, modid);
    }

    /**
     * Obtains a custom registry.
     * Note on fabric: Since there is no loading order be careful of calling this. The registry might not have been created yet
     */
    public abstract <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid);

    /**
     * Creates a custom registry.
     * On fabric the registry is created immediately
     * On forge the registry is created on RegistryEvent.NewRegistry
     */
    public abstract <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync);

    public TagKey<Item> itemTag(ResourceLocation res) {
        return TagKey.create(BuiltInRegistries.ITEM.key(), res);
    }

    public TagKey<Block> blockTag(ResourceLocation res) {
        return TagKey.create(BuiltInRegistries.BLOCK.key(), res);
    }

    public TagKey<EntityType<?>> entityTag(ResourceLocation res) {
        return TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), res);
    }

    public <T> TagKey<T> tag(ResourceKey<Registry<T>> key, ResourceLocation res) {
        return TagKey.create(key, res);
    }

    /**
     * Register a handler for this event without depending on platform loader
     */
    public abstract void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func);

}