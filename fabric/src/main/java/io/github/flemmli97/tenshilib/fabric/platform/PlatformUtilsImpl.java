package io.github.flemmli97.tenshilib.fabric.platform;

import io.github.flemmli97.tenshilib.fabric.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import io.github.flemmli97.tenshilib.platform.registry.CustomRegistryEntry;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryHandler;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryWrapper;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PlatformUtilsImpl extends PlatformUtils {

    @Override
    public SimpleRegistryWrapper<Item> items() {
        return new VanillaRegistryWrapper<>(Registry.ITEM);
    }

    @Override
    public SimpleRegistryWrapper<Block> blocks() {
        return new VanillaRegistryWrapper<>(Registry.BLOCK);
    }

    @Override
    public SimpleRegistryWrapper<BlockEntityType<?>> blocksEntities() {
        return new VanillaRegistryWrapper<>(Registry.BLOCK_ENTITY_TYPE);
    }

    @Override
    public SimpleRegistryWrapper<EntityType<?>> entities() {
        return new VanillaRegistryWrapper<>(Registry.ENTITY_TYPE);
    }

    @Override
    public SimpleRegistryWrapper<Fluid> fluids() {
        return new VanillaRegistryWrapper<>(Registry.FLUID);
    }

    @Override
    public SimpleRegistryWrapper<MobEffect> effects() {
        return new VanillaRegistryWrapper<>(Registry.MOB_EFFECT);
    }

    @Override
    public SimpleRegistryWrapper<Enchantment> enchantments() {
        return new VanillaRegistryWrapper<>(Registry.ENCHANTMENT);
    }

    @Override
    public SimpleRegistryWrapper<MenuType<?>> containers() {
        return new VanillaRegistryWrapper<>(Registry.MENU);
    }

    @Override
    public SimpleRegistryWrapper<Attribute> attributes() {
        return new VanillaRegistryWrapper<>(Registry.ATTRIBUTE);
    }

    @Override
    public SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new VanillaRegistryWrapper<>(Registry.PARTICLE_TYPE);
    }

    @Override
    public <T> SimpleRegistryWrapper<T> registry(ResourceKey<? extends Registry<T>> key) {
        return new VanillaRegistryWrapper<T>(this.registryFrom(key));
    }

    @Override
    public <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new VanillaRegistryHandler<>(registryKey, modid);
    }

    @Override
    public <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync) {
        FabricRegistryBuilder<T, DefaultedRegistry<T>> builder = FabricRegistryBuilder.createDefaulted(clss, registryKey.location(), defaultVal);
        if (saveToDisk)
            builder.attribute(RegistryAttribute.SYNCED);
        if (sync)
            builder.attribute(RegistryAttribute.SYNCED);
        builder.buildAndRegister();
        return new VanillaRegistryHandler<>(registryKey, registryKey.location().getNamespace());
    }

    @Override
    public CreativeModeTab tab(ResourceLocation label, Supplier<ItemStack> icon) {
        return FabricItemGroupBuilder.build(label, icon);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(func::apply, blocks).build();
    }

    @Override
    public void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        AOEAttackEvent.ATTACK.register(func::apply);
    }
}
