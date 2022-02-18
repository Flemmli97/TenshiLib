package io.github.flemmli97.tenshilib.fabric.platform.registry;

import io.github.flemmli97.tenshilib.fabric.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.registry.RegistryHelper;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RegistryHelperImpl extends RegistryHelper {

    @Override
    public SimpleRegistryWrapper<Item> items() {
        return new FabricRegistryWrapper<>(Registry.ITEM);
    }

    @Override
    public SimpleRegistryWrapper<Block> blocks() {
        return new FabricRegistryWrapper<>(Registry.BLOCK);
    }

    @Override
    public SimpleRegistryWrapper<EntityType<?>> entities() {
        return new FabricRegistryWrapper<>(Registry.ENTITY_TYPE);
    }

    @Override
    public SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new FabricRegistryWrapper<>(Registry.PARTICLE_TYPE);
    }

    @Override
    public void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        AOEAttackEvent.ATTACK.register(func::apply);
    }
}
