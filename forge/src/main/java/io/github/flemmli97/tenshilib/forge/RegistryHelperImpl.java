package io.github.flemmli97.tenshilib.forge;

import io.github.flemmli97.tenshilib.SimpleRegistryWrapper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHelperImpl {

    public static SimpleRegistryWrapper<Item> items() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ITEMS);
    }

    public static SimpleRegistryWrapper<Block> blocks() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.BLOCKS);
    }

    public static SimpleRegistryWrapper<EntityType<?>> entities() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ENTITIES);
    }

    public static SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.PARTICLE_TYPES);
    }
}
