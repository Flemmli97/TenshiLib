package io.github.flemmli97.tenshilib.platform;

import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public abstract class CrossPlatRegistry {

    public static final CrossPlatRegistry INSTANCE = InitUtil.getPlatformInstance(CrossPlatRegistry.class,
            "io.github.flemmli97.tenshilib.fabric.platform.CrossPlatRegistryImpl",
            "io.github.flemmli97.tenshilib.neoforge.platform.CrossPlatRegistryImpl");

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
     * In most cases this shouldn't be used
     */
    public abstract <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid);

    /**
     * Creates a custom registry.
     * On fabric the registry is created immediately
     * On (neo)forge the registry is created on RegistryEvent.NewRegistry
     *
     * @param registryRef A callback to the newly created Registry
     */
    public abstract <T> PlatformRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync, Consumer<Registry<T>> registryRef);

    /**
     * Register a handler for this event without depending on platform loader
     */
    public abstract void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func);

}