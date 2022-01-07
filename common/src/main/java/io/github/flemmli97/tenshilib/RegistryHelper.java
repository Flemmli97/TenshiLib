package io.github.flemmli97.tenshilib;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegistryHelper {

    @ExpectPlatform
    public static SimpleRegistryWrapper<Item> items() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<Block> blocks() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<EntityType<?>> entities() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<ParticleType<?>> particles() {
        throw new AssertionError();
    }
}
