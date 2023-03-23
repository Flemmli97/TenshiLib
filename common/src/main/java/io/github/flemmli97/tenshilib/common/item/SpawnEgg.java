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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cause MC SpawnEggItem is not made for extension
 */
public class SpawnEgg extends Item {

    private static final Map<Supplier<? extends EntityType<?>>, SpawnEgg> EGGSSUP = Maps.newIdentityHashMap();

    protected static final DispenseItemBehavior def = (blockSource, stack) -> {
        Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
        spawnEntity(blockSource.getLevel(), null, stack, blockSource.getPos().relative(direction), MobSpawnType.DISPENSER, false, direction != Direction.UP, false);
        stack.shrink(1);
        return stack;
    };

    private final int primaryColor;
    private final int secondaryColor;
    private final Supplier<? extends EntityType<?>> typeIn;

    public SpawnEgg(Supplier<? extends EntityType<?>> type, int primary, int secondary, Properties props) {
        super(props);
        this.typeIn = type;
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.onInit(type);
    }

    protected void onInit(Supplier<? extends EntityType<?>> type) {
        EGGSSUP.put(type, this);
    }

    public static Iterable<SpawnEgg> getEggs() {
        return Iterables.unmodifiableIterable(EGGSSUP.values());
    }

    /**
     * IDK how to find it faster since its saved as suppliers
     */
    public static Optional<SpawnEgg> fromType(EntityType<?> type) {
        return EGGSSUP.entrySet().stream().filter(e -> e.getKey().get() == type).findFirst().map(Map.Entry::getValue);
    }

    public static Optional<SpawnEgg> fromID(ResourceLocation id) {
        return EGGSSUP.entrySet().stream().filter(e -> PlatformUtils.INSTANCE.entities().getIDFrom(e.getKey().get()).equals(id)).findFirst().map(Map.Entry::getValue);
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
        return def;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack), Component.translatable(this.getType(stack.getTag()).getDescriptionId()));
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
            if (tile instanceof SpawnerBlockEntity spawnerBlock) {
                EntityType<?> entitytype1 = this.getType(stack.getTag());
                spawnerBlock.setEntityId(entitytype1, ctx.getLevel().getRandom());
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
        Entity e = type.create(world, stack.getTag(), EntityType.createDefaultStackConfig(world, stack, player), pos, reason, updateLocation, doCollisionOffset);
        if (e != null) {
            if (!item.onEntitySpawned(e, stack, player) || (forgeCheck && e instanceof Mob && EventCalls.INSTANCE.specialSpawnCall((Mob) e, world, pos.getX(), pos.getY(), pos.getZ(), null, reason)))
                return null;
            world.addFreshEntityWithPassengers(e);
        }
        return e;
    }

    public int getColor(ItemStack stack, int i) {
        return i == 0 ? this.primaryColor : this.secondaryColor;
    }

    public EntityType<?> getType(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(EntityType.ENTITY_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag compoundnbt = nbt.getCompound(EntityType.ENTITY_TAG);
            if (compoundnbt.contains("id", Tag.TAG_STRING)) {
                EntityType<?> type = PlatformUtils.INSTANCE.entities().getFromId(new ResourceLocation(compoundnbt.getString("id")));
                return type != null ? type : this.typeIn.get();
            }
        }

        return this.typeIn.get();
    }

    public boolean hasType(@Nullable CompoundTag nbt, EntityType<?> type) {
        return Objects.equals(this.getType(nbt), type);
    }

    public Optional<Mob> spawnBaby(Player player, Mob entity, EntityType<? extends Mob> type, ServerLevel world, Vec3 pos, ItemStack stack) {
        if (!this.hasType(stack.getTag(), type)) {
            return Optional.empty();
        } else {
            Mob mob;
            if (entity instanceof AgeableMob) {
                mob = ((AgeableMob) entity).getBreedOffspring(world, (AgeableMob) entity);
            } else {
                mob = type.create(world);
            }

            if (mob == null) {
                return Optional.empty();
            } else {
                mob.setBaby(true);
                if (!mob.isBaby()) {
                    return Optional.empty();
                } else {
                    mob.moveTo(pos.x(), pos.y(), pos.z(), 0.0F, 0.0F);
                    if (!this.onEntitySpawned(mob, stack, player))
                        return Optional.empty();
                    world.addFreshEntityWithPassengers(mob);
                    Component comp = this.getEntityName(stack);
                    if (comp != null) {
                        mob.setCustomName(comp);
                    }

                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }

                    return Optional.of(mob);
                }
            }
        }
    }
}
