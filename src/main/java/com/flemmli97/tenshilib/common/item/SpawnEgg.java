package com.flemmli97.tenshilib.common.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cause MC SpawnEggItem is not made for extension
 */
public class SpawnEgg extends Item {

    private static final Map<Supplier<? extends EntityType<?>>, SpawnEgg> EGGSSUP = Maps.newIdentityHashMap();

    protected static final IDispenseItemBehavior def = (blockSource, stack) -> {
        Direction direction = blockSource.getBlockState().get(DispenserBlock.FACING);
        spawnEntity(blockSource.getWorld(), null, stack, blockSource.getBlockPos().offset(direction), SpawnReason.DISPENSER, false, direction != Direction.UP, false);
        stack.shrink(1);
        return stack;
    };

    private final int primaryColor;
    private final int secondaryColor;
    private final Supplier<? extends EntityType<?>> typeIn;

    public SpawnEgg(Supplier<? extends EntityType<?>> type, int primary, int secondary, Item.Properties props) {
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

    /**
     * Additional hook to modify the entity
     */
    public boolean onEntitySpawned(Entity e, ItemStack stack, @Nullable PlayerEntity player) {
        return true;
    }

    public ITextComponent getEntityName(ItemStack stack) {
        return stack.hasDisplayName() ? stack.getDisplayName() : null;
    }

    public IDispenseItemBehavior dispenser() {
        return def;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent(this.getTranslationKey(stack), new TranslationTextComponent(this.getType(stack.getTag()).getTranslationKey()));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        } else {
            ItemStack stack = ctx.getItem();
            BlockPos blockpos = ctx.getPos();
            Direction direction = ctx.getFace();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.isIn(Blocks.SPAWNER)) {
                TileEntity tileentity = world.getTileEntity(blockpos);
                if (tileentity instanceof MobSpawnerTileEntity) {
                    AbstractSpawner abstractspawner = ((MobSpawnerTileEntity) tileentity).getSpawnerBaseLogic();
                    EntityType<?> entitytype1 = this.getType(stack.getTag());
                    abstractspawner.setEntityType(entitytype1);
                    tileentity.markDirty();
                    world.notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
                    stack.shrink(1);
                    return ActionResultType.CONSUME;
                }
            }

            BlockPos blockpos1 = blockstate.getCollisionShape(world, blockpos).isEmpty() ? blockpos : blockpos.offset(direction);

            Entity e = spawnEntity((ServerWorld) world, ctx.getPlayer(), stack, blockpos1, SpawnReason.SPAWN_EGG, true, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);
            if (e != null) {
                stack.shrink(1);
            }
            return ActionResultType.CONSUME;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        BlockRayTraceResult raytraceresult = rayTrace(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (raytraceresult.getType() != RayTraceResult.Type.BLOCK) {
            return ActionResult.pass(stack);
        } else if (!(world instanceof ServerWorld)) {
            return ActionResult.success(stack);
        } else {
            BlockRayTraceResult blockraytraceresult = raytraceresult;
            BlockPos blockpos = blockraytraceresult.getPos();
            if (!(world.getBlockState(blockpos).getBlock() instanceof FlowingFluidBlock)) {
                return ActionResult.pass(stack);
            } else if (world.isBlockModifiable(player, blockpos) && player.canPlayerEdit(blockpos, blockraytraceresult.getFace(), stack)) {
                Entity e = spawnEntity((ServerWorld) world, player, stack, blockpos, SpawnReason.SPAWN_EGG, true, true, false);
                if (e != null) {
                    if (!player.abilities.isCreativeMode)
                        stack.shrink(1);
                    player.addStat(Stats.ITEM_USED.get(this));
                    return ActionResult.consume(stack);
                }
                return ActionResult.pass(stack);
            } else {
                return ActionResult.fail(stack);
            }
        }
    }

    public static Entity spawnEntity(ServerWorld world, PlayerEntity player, ItemStack stack, BlockPos pos, SpawnReason reason, boolean forgeCheck, boolean updateLocation, boolean doCollisionOffset) {
        if (!(stack.getItem() instanceof SpawnEgg))
            return null;
        SpawnEgg item = (SpawnEgg) stack.getItem();
        EntityType<?> type = item.getType(stack.getTag());
        Entity e = type.create(world, stack.getTag(), item.getEntityName(stack), player, pos, reason, updateLocation, doCollisionOffset);
        if (e != null) {
            if (!item.onEntitySpawned(e, stack, player) || (forgeCheck && e instanceof MobEntity && ForgeEventFactory.doSpecialSpawn((MobEntity) e, world, pos.getX(), pos.getY(), pos.getZ(), null, reason)))
                return null;
            world.spawnEntityAndPassengers(e);
        }
        return e;
    }

    public int getColor(int i) {
        return i == 0 ? this.primaryColor : this.secondaryColor;
    }

    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT compoundnbt = nbt.getCompound("EntityTag");
            if (compoundnbt.contains("id", Constants.NBT.TAG_STRING)) {
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(compoundnbt.getString("id")));
                return type != null ? type : this.typeIn.get();
            }
        }

        return this.typeIn.get();
    }

    public boolean hasType(@Nullable CompoundNBT nbt, EntityType<?> type) {
        return Objects.equals(this.getType(nbt), type);
    }

    public Optional<MobEntity> spawnBaby(PlayerEntity player, MobEntity entity, EntityType<? extends MobEntity> type, ServerWorld world, Vector3d pos, ItemStack stack) {
        if (!this.hasType(stack.getTag(), type)) {
            return Optional.empty();
        } else {
            MobEntity mob;
            if (entity instanceof AgeableEntity) {
                mob = ((AgeableEntity) entity).createChild(world, (AgeableEntity) entity);
            } else {
                mob = type.create(world);
            }

            if (mob == null) {
                return Optional.empty();
            } else {
                mob.setChild(true);
                if (!mob.isChild()) {
                    return Optional.empty();
                } else {
                    mob.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                    if (!this.onEntitySpawned(mob, stack, player))
                        return Optional.empty();
                    world.spawnEntityAndPassengers(mob);
                    ITextComponent comp = this.getEntityName(stack);
                    if (comp != null) {
                        mob.setCustomName(comp);
                    }

                    if (!player.abilities.isCreativeMode) {
                        stack.shrink(1);
                    }

                    return Optional.of(mob);
                }
            }
        }
    }
}
