package io.github.flemmli97.tenshilib.forge.platform.registry;

import io.github.flemmli97.tenshilib.forge.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.RegistryHelper;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.List;
import java.util.function.Consumer;

public class RegistryHelperImpl extends RegistryHelper {

    @Override
    public <T> ResourceLocation idOf(T t, ResourceKey<Registry<T>> key) {
        if (t instanceof IForgeRegistry<?> f)
            return f.getRegistryName();
        return super.idOf(t, key);
    }

    @Override
    public SimpleRegistryWrapper<Item> items() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ITEMS);
    }

    @Override
    public SimpleRegistryWrapper<Block> blocks() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.BLOCKS);
    }

    @Override
    public SimpleRegistryWrapper<EntityType<?>> entities() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ENTITIES);
    }

    @Override
    public SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.PARTICLE_TYPES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        IForgeRegistry<?> reg = RegistryManager.ACTIVE.getRegistry(key.location());
        if (reg != null)
            return ((PlatformRegistry<T>) new ForgeRegistryHandler<>(DeferredRegister.create(reg, modid)));
        return super.of(key, modid);
    }

    @Override
    public void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        Consumer<AOEAttackEvent> cons = event -> {
            if (!func.apply(event.getPlayer(), event.usedItem, event.attackList()))
                event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(cons);
    }
}
