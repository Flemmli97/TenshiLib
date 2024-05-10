package io.github.flemmli97.tenshilib.common.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Uses a supplier for the EntityType. Also adds various hooks etc. during spawning
 */
public class SpawnEgg extends SpawnEggItem {

    private static final Map<Supplier<? extends EntityType<? extends Mob>>, SpawnEgg> EGGS_SUP = Maps.newIdentityHashMap();
    private static final Map<EntityType<? extends Mob>, SpawnEgg> EGGS = Maps.newIdentityHashMap();
    private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = fetchMapFromSpawnEgg();
    private static boolean resolved;
    private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");

    protected static final DispenseItemBehavior DEF = (blockSource, stack) -> {
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        spawnEntity(blockSource.level(), null, stack, blockSource.pos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
        stack.shrink(1);
        blockSource.level().gameEvent(GameEvent.ENTITY_PLACE, blockSource.pos(), GameEvent.Context.of(blockSource.state()));
        return stack;
    };

    private final Supplier<? extends EntityType<?>> type;

    public SpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primary, int secondary, Properties props) {
        super(null, primary, secondary, props);
        BY_ID.remove(null);
        this.type = type;
        this.onInit(type);
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityType<? extends Mob>, SpawnEggItem> fetchMapFromSpawnEgg() {
        for (Field f : SpawnEggItem.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Map.class.isAssignableFrom(f.getType())) {
                try {
                    f.setAccessible(true);
                    Object obj = f.get(null);
                    if (obj instanceof IdentityHashMap<?, ?>)
                        return (Map<EntityType<? extends Mob>, SpawnEggItem>) f.get(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException("Couldn't find spawnegg map");
    }

    /**
     * Resolve the suppliers. Adding to vanilla map if applicable
     */
    public static void resolveEggs() {
        if (resolved)
            return;
        resolved = true;
        EGGS_SUP.forEach((type, egg) -> {
            if (egg.addToDefaultSpawneggs())
                BY_ID.put(type.get(), egg);
            EGGS.put(type.get(), egg);
        });
    }

    public static Iterable<SpawnEgg> getEggs() {
        return Iterables.unmodifiableIterable(EGGS_SUP.values());
    }

    public static Optional<SpawnEgg> fromType(EntityType<?> type) {
        return Optional.ofNullable(EGGS.get(type));
    }

    public static Optional<SpawnEgg> fromID(ResourceLocation id) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).flatMap(SpawnEgg::fromType);
    }

    protected void onInit(Supplier<? extends EntityType<? extends Mob>> type) {
        EGGS_SUP.put(type, this);
    }

    /**
     * Additional hook to modify the entity
     */
    public boolean onEntitySpawned(Entity e, ItemStack stack, @Nullable Player player) {
        return true;
    }

    public Component getEntityName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : null;
    }

    public DispenseItemBehavior dispenser() {
        return DEF;
    }

    public boolean addToDefaultSpawneggs() {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack), Component.translatable(this.getType(stack).getDescriptionId()));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = ctx.getItemInHand();
            BlockPos blockpos = ctx.getClickedPos();
            Direction direction = ctx.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockEntity = level.getBlockEntity(blockpos);
            InteractionResult onBlock = this.onBlockUse(stack, blockpos, blockstate, blockEntity);
            if (onBlock != InteractionResult.PASS)
                return onBlock;
            if (blockEntity instanceof SpawnerBlockEntity spawner) {
                CompoundTag nbt = new CompoundTag();
                spawner.getSpawner().save(nbt);
                nbt.remove("SpawnPotentials");
                nbt.remove(BaseSpawner.SPAWN_DATA_TAG);
                SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, new SpawnData(stack.get(DataComponents.ENTITY_DATA).copyTag(), Optional.empty(), Optional.empty()))
                        .resultOrPartial(string -> TenshiLib.LOGGER.warn("Invalid SpawnData: {}", string))
                        .ifPresent(t -> nbt.put(BaseSpawner.SPAWN_DATA_TAG, t));
                spawner.getSpawner().load(blockEntity.getLevel(), blockEntity.getBlockPos(), nbt);
                spawner.setChanged();
                ctx.getLevel().sendBlockUpdated(ctx.getClickedPos(), blockstate, blockstate, 3);
                return InteractionResult.SUCCESS;
            }
            BlockPos blockPos = blockstate.getCollisionShape(level, blockpos).isEmpty() ? blockpos : blockpos.relative(direction);
            Entity entity = spawnEntity((ServerLevel) level, ctx.getPlayer(), stack, blockPos, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockPos) && direction == Direction.UP);
            if (entity != null) {
                stack.shrink(1);
                if (ctx.getPlayer() != null) {
                    ctx.getPlayer().awardStat(Stats.ITEM_USED.get(this));
                }
                level.gameEvent(ctx.getPlayer(), GameEvent.ENTITY_PLACE, entity.position());
            }
            return InteractionResult.CONSUME;
        }
    }

    public InteractionResult onBlockUse(ItemStack stack, BlockPos pos, BlockState state, @Nullable BlockEntity tile) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        } else if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        } else {
            BlockPos blockpos = hitResult.getBlockPos();
            if (!(level.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                return InteractionResultHolder.pass(stack);
            } else if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos, hitResult.getDirection(), stack)) {
                Entity entity = spawnEntity(serverLevel, player, stack, blockpos, MobSpawnType.SPAWN_EGG, true, false);
                if (entity != null) {
                    if (!player.isCreative())
                        stack.shrink(1);
                    player.awardStat(Stats.ITEM_USED.get(this));
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
                    return InteractionResultHolder.consume(stack);
                }
                return InteractionResultHolder.pass(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }
    }

    @Override
    public EntityType<?> getType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        return !customData.isEmpty() ? customData.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(this.type.get()) : this.type.get();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.type.get().requiredFeatures();
    }

    public static Entity spawnEntity(ServerLevel level, Player player, ItemStack stack, BlockPos pos, MobSpawnType reason, boolean updateLocation, boolean doCollisionOffset) {
        if (!(stack.getItem() instanceof SpawnEgg item))
            return null;
        EntityType<?> type = item.getType(stack);
        Entity entity = type.create(level, EntityType.createDefaultStackConfig(level, stack, player), pos, reason, updateLocation, doCollisionOffset);
        if (entity != null) {
            if (!item.onEntitySpawned(entity, stack, player))
                return null;
            level.addFreshEntityWithPassengers(entity);
        }
        return entity;
    }

    public int getColor(ItemStack stack, int tintIndex) {
        return this.getColor(tintIndex);
    }
}
