package io.github.flemmli97.tenshilib.common.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    protected static final DispenseItemBehavior DEF = (blockSource, stack) -> {
        Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
        spawnEntity(blockSource.getLevel(), null, stack, blockSource.getPos().relative(direction), MobSpawnType.DISPENSER, false, direction != Direction.UP, false);
        stack.shrink(1);
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

    protected void onInit(Supplier<? extends EntityType<? extends Mob>> type) {
        EGGS_SUP.put(type, this);
    }

    public static Iterable<SpawnEgg> getEggs() {
        return Iterables.unmodifiableIterable(EGGS_SUP.values());
    }

    /**
     * IDK how to find it faster since its saved as suppliers
     */
    public static Optional<SpawnEgg> fromType(EntityType<?> type) {
        return Optional.ofNullable(EGGS.get(type));
    }

    public static Optional<SpawnEgg> fromID(ResourceLocation id) {
        return PlatformUtils.INSTANCE.entities().getOptionalFromId(id).flatMap(SpawnEgg::fromType);
    }

    /**
     * Additional hook to modify the entity
     */
    public boolean onEntitySpawned(Entity e, ItemStack stack, @Nullable Player player) {
        return true;
    }

    public Component getEntityName(ItemStack stack) {
        return stack.hasCustomHoverName() ? stack.getHoverName() : null;
    }

    public DispenseItemBehavior dispenser() {
        return DEF;
    }

    public boolean addToDefaultSpawneggs() {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return new TranslatableComponent(this.getDescriptionId(stack), new TranslatableComponent(this.getType(stack.getTag()).getDescriptionId()));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = ctx.getItemInHand();
            BlockPos blockpos = ctx.getClickedPos();
            Direction direction = ctx.getClickedFace();
            BlockState blockstate = world.getBlockState(blockpos);
            BlockEntity tile = world.getBlockEntity(blockpos);
            InteractionResult onBlock = this.onBlockUse(stack, blockpos, blockstate, tile);
            if (onBlock != InteractionResult.PASS)
                return onBlock;
            if (tile instanceof SpawnerBlockEntity) {
                BaseSpawner abstractspawner = ((SpawnerBlockEntity) tile).getSpawner();
                EntityType<?> entitytype1 = this.getType(stack.getTag());
                abstractspawner.setEntityId(entitytype1);
                tile.setChanged();
                world.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                stack.shrink(1);
                return InteractionResult.CONSUME;
            }

            BlockPos blockpos1 = blockstate.getCollisionShape(world, blockpos).isEmpty() ? blockpos : blockpos.relative(direction);

            Entity e = spawnEntity((ServerLevel) world, ctx.getPlayer(), stack, blockpos1, MobSpawnType.SPAWN_EGG, true, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);
            if (e != null) {
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
    }

    public InteractionResult onBlockUse(ItemStack stack, BlockPos pos, BlockState state, @Nullable BlockEntity tile) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult raytraceresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
        if (raytraceresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        } else if (!(world instanceof ServerLevel)) {
            return InteractionResultHolder.success(stack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!(world.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                return InteractionResultHolder.pass(stack);
            } else if (world.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos, raytraceresult.getDirection(), stack)) {
                Entity e = spawnEntity((ServerLevel) world, player, stack, blockpos, MobSpawnType.SPAWN_EGG, true, true, false);
                if (e != null) {
                    if (!player.isCreative())
                        stack.shrink(1);
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.consume(stack);
                }
                return InteractionResultHolder.pass(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }
    }

    public static Entity spawnEntity(ServerLevel world, Player player, ItemStack stack, BlockPos pos, MobSpawnType reason, boolean forgeCheck, boolean updateLocation, boolean doCollisionOffset) {
        if (!(stack.getItem() instanceof SpawnEgg item))
            return null;
        EntityType<?> type = item.getType(stack.getTag());
        Entity e = type.create(world, stack.getTag(), item.getEntityName(stack), player, pos, reason, updateLocation, doCollisionOffset);
        if (e != null) {
            if (!item.onEntitySpawned(e, stack, player) || (forgeCheck && e instanceof Mob && EventCalls.INSTANCE.specialSpawnCall((Mob) e, world, pos.getX(), pos.getY(), pos.getZ(), null, reason)))
                return null;
            world.addFreshEntityWithPassengers(e);
        }
        return e;
    }

    public int getColor(ItemStack stack, int i) {
        return super.getColor(i);
    }

    @Override
    public EntityType<?> getType(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains("EntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag compoundnbt = nbt.getCompound("EntityTag");
            if (compoundnbt.contains("id", Tag.TAG_STRING)) {
                EntityType<?> type = PlatformUtils.INSTANCE.entities().getFromId(new ResourceLocation(compoundnbt.getString("id")));
                return type != null ? type : this.type.get();
            }
        }

        return this.type.get();
    }
}
