package io.github.flemmli97.tenshilib.platform.registry;

import io.github.flemmli97.tenshilib.EarlyPlatformInit;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class RegistryHelper {

    protected static RegistryHelper INSTANCE;

    /**
     * Using this cause we need the implementation earlier than its possible to init it normally.
     */
    public static RegistryHelper instance() {
        if (INSTANCE == null) {
            INSTANCE = EarlyPlatformInit.init();
        }
        return INSTANCE;
    }

    public abstract SimpleRegistryWrapper<Item> items();

    public abstract SimpleRegistryWrapper<Block> blocks();

    public abstract SimpleRegistryWrapper<EntityType<?>> entities();

    public abstract SimpleRegistryWrapper<ParticleType<?>> particles();

    @SuppressWarnings("unchecked")
    public <T> ResourceLocation idOf(T t, ResourceKey<Registry<T>> key) {
        return ((Registry<T>) Registry.REGISTRY.get(key.location())).getKey(t);
    }

    @SuppressWarnings("unchecked")
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        Registry<T> reg = (Registry<T>) Registry.REGISTRY.get(key.location());
        if (reg == null)
            throw new NullPointerException("Failed to get a corresponding recipe for " + key);
        return new VanillaRegistryHandler<>(reg, modid);
    }

    /**
     * Register an handler for this event without depending on platform loader
     */
    public abstract void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func);

}