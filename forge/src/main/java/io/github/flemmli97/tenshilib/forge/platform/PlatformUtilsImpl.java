package io.github.flemmli97.tenshilib.forge.platform;

import io.github.flemmli97.tenshilib.forge.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.forge.platform.registry.ForgeRegistryHandler;
import io.github.flemmli97.tenshilib.forge.platform.registry.ForgeRegistryWrapper;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import io.github.flemmli97.tenshilib.platform.registry.CustomRegistryEntry;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryWrapper;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformUtilsImpl extends PlatformUtils {

    @Override
    public <T> ResourceLocation idOf(T t, ResourceKey<? extends Registry<T>> key) {
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
    public SimpleRegistryWrapper<BlockEntityType<?>> blocksEntities() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.BLOCK_ENTITIES);
    }

    @Override
    public SimpleRegistryWrapper<EntityType<?>> entities() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ENTITIES);
    }

    @Override
    public SimpleRegistryWrapper<Fluid> fluids() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.FLUIDS);
    }

    @Override
    public SimpleRegistryWrapper<MobEffect> effects() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.MOB_EFFECTS);
    }

    @Override
    public SimpleRegistryWrapper<Enchantment> enchantments() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ENCHANTMENTS);
    }

    @Override
    public SimpleRegistryWrapper<MenuType<?>> containers() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.CONTAINERS);
    }

    @Override
    public SimpleRegistryWrapper<Attribute> attributes() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ATTRIBUTES);
    }

    @Override
    public SimpleRegistryWrapper<ParticleType<?>> particles() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.PARTICLE_TYPES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> SimpleRegistryWrapper<T> registry(ResourceKey<? extends Registry<T>> key) {
        return this.forgeRegistryFrom(key).map(reg -> ((SimpleRegistryWrapper<T>) new ForgeRegistryWrapper<>(reg)))
                .orElseGet(() -> new VanillaRegistryWrapper<T>(this.registryFrom(key)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return this.forgeRegistryFrom(key).map(reg -> ((PlatformRegistry<T>) new ForgeRegistryHandler<>(DeferredRegister.create(reg, modid))))
                .orElseGet(() -> super.of(key, modid));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new ForgeRegistryHandler<>(DeferredRegister.create((Class) clss, modid));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync) {
        DeferredRegister r = DeferredRegister.create((Class) clss, registryKey.location().getNamespace());
        r.makeRegistry(registryKey.location().getPath(), () -> new RegistryBuilder<>().setDefaultKey(defaultVal));
        return new ForgeRegistryHandler<>(r);
    }

    @Override
    public CreativeModeTab tab(ResourceLocation label, Supplier<ItemStack> icon) {
        return new CreativeModeTab(String.format("%s.%s", label.getNamespace(), label.getPath())) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
        return BlockEntityType.Builder.of(func::apply, blocks).build(null);
    }

    protected Optional<IForgeRegistry<?>> forgeRegistryFrom(ResourceKey<? extends Registry<?>> key) {
        return Optional.ofNullable(RegistryManager.ACTIVE.getRegistry(key.location()));
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
