package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.SimpleRegistryWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegistryHelperImpl {

    public static SimpleRegistryWrapper<Item> items() {
        return new FabricRegistryWrapper<>(Registry.ITEM);
    }

    public static SimpleRegistryWrapper<Block> blocks() {
        return new FabricRegistryWrapper<>(Registry.BLOCK);
    }

    public static SimpleRegistryWrapper<EntityType<?>> entities() {
        return new FabricRegistryWrapper<>(Registry.ENTITY_TYPE);
    }

    public static SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new FabricRegistryWrapper<>(Registry.PARTICLE_TYPE);
    }
}
