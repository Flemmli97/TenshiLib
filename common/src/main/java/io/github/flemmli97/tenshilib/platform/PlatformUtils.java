package io.github.flemmli97.tenshilib.platform;

import io.github.flemmli97.tenshilib.platform.registry.CustomRegistryEntry;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.SimpleRegistryWrapper;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

public abstract class PlatformUtils {

    public static final PlatformUtils INSTANCE = InitUtil.getPlatformInstance(PlatformUtils.class,
            "io.github.flemmli97.tenshilib.fabric.platform.PlatformUtilsImpl",
            "io.github.flemmli97.tenshilib.forge.platform.PlatformUtilsImpl");

    public abstract SimpleRegistryWrapper<Item> items();

    public abstract SimpleRegistryWrapper<Block> blocks();

    public abstract SimpleRegistryWrapper<BlockEntityType<?>> blocksEntities();

    public abstract SimpleRegistryWrapper<EntityType<?>> entities();

    public abstract SimpleRegistryWrapper<Fluid> fluids();

    public abstract SimpleRegistryWrapper<MobEffect> effects();

    public abstract SimpleRegistryWrapper<Enchantment> enchantments();

    public abstract SimpleRegistryWrapper<MenuType<?>> containers();

    public abstract SimpleRegistryWrapper<Attribute> attributes();

    public abstract SimpleRegistryWrapper<ParticleType<?>> particles();

    /**
     * Gets a registry from the resourcekey. Does a lookup in all the registered registries so
     * other versions should be used when possible
     */
    public abstract <T> SimpleRegistryWrapper<T> registry(ResourceKey<? extends Registry<T>> key);

    public <T> ResourceLocation idOf(T t, ResourceKey<? extends Registry<T>> key) {
        return this.registryFrom(key).getKey(t);
    }

    /**
     * Creates a registry handler for the matching key.
     * The registry needs to exist for the key else an exeption will be thrown.
     * Registering to custom registries is not possible since T needs to also implement IForgeRegistryEntry on forge
     */
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new VanillaRegistryHandler<>(key, modid);
    }

    /**
     * Obtains a custom registry.
     * Note on fabric: Since there is no loading order be careful of calling this. The registry might not have been created yet
     */
    public abstract <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceLocation registryID, String modid);

    /**
     * Creates a custom registry.
     * On fabric the registry is created immediately
     * On forge the registry is created on RegistryEvent.NewRegistry
     */
    public abstract <T extends CustomRegistryEntry<T>> PlatformRegistry<T> customRegistry(Class<T> clss, ResourceLocation res, ResourceLocation defaultVal, boolean saveToDisk, boolean sync);

    @SuppressWarnings("unchecked")
    protected <T> Registry<T> registryFrom(ResourceKey<? extends Registry<T>> key) {
        Registry<?> reg = Registry.REGISTRY.get(key.location());
        if (reg == null)
            throw new NullPointerException("Failed to get a corresponding registry for " + key);
        return (Registry<T>) reg;
    }

    public abstract CreativeModeTab tab(ResourceLocation label, Supplier<ItemStack> icon);

    public abstract <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> func, Block... blocks);

    public TagKey<Item> itemTag(ResourceLocation res) {
        return TagKey.create(Registry.ITEM_REGISTRY, res);
    }

    public TagKey<Block> blockTag(ResourceLocation res) {
        return TagKey.create(Registry.BLOCK_REGISTRY, res);
    }

    public TagKey<EntityType<?>> entityTag(ResourceLocation res) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, res);
    }

    public <T> TagKey<T> tag(ResourceKey<Registry<T>> key, ResourceLocation res) {
        return TagKey.create(key, res);
    }

    /**
     * Register an handler for this event without depending on platform loader
     */
    public abstract void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func);

}