package io.github.flemmli97.tenshilib.common.entity;

import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityProjectile extends Projectile {

    private Entity shooter;

    protected boolean inGround;
    protected int ticksInGround, livingTicks;
    public final List<UUID> attackedEntities = new ArrayList<>();
    public final List<UUID> checkedEntities = new ArrayList<>();

    private BlockState ground;
    private BlockPos groundPos;

    protected static final EntityDataAccessor<Optional<UUID>> shooterUUID = SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.OPTIONAL_UUID);

    public EntityProjectile(EntityType<? extends EntityProjectile> type, Level world) {
        super(type, world);
    }

    public EntityProjectile(EntityType<? extends EntityProjectile> type, Level world, double x, double y, double z) {
        this(type, world);
        this.setPos(x, y, z);
    }

    public EntityProjectile(EntityType<? extends EntityProjectile> type, Level world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1, shooter.getZ());
        this.shooter = shooter;
        this.entityData.set(shooterUUID, Optional.of(shooter.getUUID()));
        this.setRot(shooter.getYRot(), shooter.getXRot());
        this.onUpdateOwner();
    }

    public boolean isPiercing() {
        return false;
    }

    public int maxPierceAmount() {
        return -1;
    }

    /**
     * Doesnt work properly yet
     */
    public float radius() {
        return 0;
    }

    public int livingTicks() {
        return this.livingTicks;
    }

    public int livingTickMax() {
        return 6000;
    }

    public boolean canHitShooter() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(shooterUUID, Optional.empty());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;

        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -Mth.sin(rotationYawIn * 0.017453292F) * Mth.cos(rotationPitchIn * 0.017453292F);
        float f1 = -Mth.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = Mth.cos(rotationYawIn * 0.017453292F) * Mth.cos(rotationPitchIn * 0.017453292F);
        this.shoot(f, f1, f2, velocity, inaccuracy);
        Vec3 throwerMotion = entityThrower.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(throwerMotion.x, entityThrower.isOnGround() ? 0.0D : throwerMotion.y, throwerMotion.z));
        this.getDeltaMovement().add(throwerMotion.x, 0, throwerMotion.z);
    }

    /**
     * Shoots directly at the given position
     */
    public void shootAtPosition(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 dir = new Vec3(x - this.getX(), y - this.getY(), z - this.getZ());
        this.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    public void shootAtEntity(Entity target, float velocity, float inaccuracy, float yOffsetModifier) {
        this.shootAtEntity(target, velocity, inaccuracy, yOffsetModifier, 0.33);
    }

    /**
     * Shoot at the given entity. Unlike #shootAtPosition this doesnt shoot directly where the entity is but rather through it (like arrows)
     *
     * @param yOffsetModifier Modifies the offset of the y motion based on distance to target. Vanilla arrows use 0.2
     */
    public void shootAtEntity(Entity target, float velocity, float inaccuracy, float yOffsetModifier, double heighMod) {
        Vec3 dir = (new Vec3(target.getX() - this.getX(), target.getY(heighMod) - this.getY(), target.getZ() - this.getZ()));
        double l = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.shoot(dir.x, dir.y + l * yOffsetModifier, dir.z, velocity, inaccuracy);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vector3d = (new Vec3(x, y, z)).normalize().add(this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy).scale(velocity);
        this.setDeltaMovement(vector3d);
        double f = Math.sqrt(horizontalMag(vector3d));
        this.setYRot((float) (Mth.atan2(vector3d.x, vector3d.z) * (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vector3d.y, f) * (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.ticksInGround = 0;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double f = Math.sqrt(x * x + z * z);
            this.setXRot((float) (Mth.atan2(y, f) * (180F / (float) Math.PI)));
            this.setYRot((float) (Mth.atan2(x, z) * (180F / (float) Math.PI)));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    public void setInGround(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!state.getMaterial().isSolid()) {
            this.inGround = false;
            return;
        }
        this.inGround = true;
        this.groundPos = pos;
        this.ground = state;
    }

    @Override
    public void tick() {
        super.tick();
        this.livingTicks++;
        if (!this.level.isClientSide && this.livingTicks > this.livingTickMax())
            this.remove(RemovalReason.KILLED);
        Vec3 motion = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double f = Math.sqrt(horizontalMag(motion));
            this.setXRot((float) (Mth.atan2(motion.x, motion.z) * (180F / (float) Math.PI)));
            this.setYRot((float) (Mth.atan2(motion.y, f) * (180F / (float) Math.PI)));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
        }

        BlockState inState = this.level.getBlockState(this.blockPosition());
        if (this.inGround) {
            if (inState != this.ground && this.noGround())
                this.resetInGround();
            else if (!this.level.isClientSide) {
                ++this.ticksInGround;
                if (this.ticksInGround == 1200)
                    this.remove(RemovalReason.KILLED);
            }
            return;
        }

        if (!this.level.isClientSide) {
            this.doCollision();
        }
        this.moveEntity();
    }

    public void moveEntity() {
        Vec3 motion = this.getDeltaMovement();
        double newX = this.getX() + motion.x;
        double newY = this.getY() + motion.y;
        double newZ = this.getZ() + motion.z;

        double f = Math.sqrt(horizontalMag(motion));
        this.setYRot(this.updateRotation(this.yRotO, (float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI))));
        this.setXRot(this.updateRotation(this.xRotO, (float) (Mth.atan2(motion.y, f) * (double) (180F / (float) Math.PI))));

        boolean water = this.isInWater();
        if (water) {
            for (int i = 0; i < 4; ++i) {
                this.level.addParticle(ParticleTypes.BUBBLE, this.getX() * 0.25D, this.getY() * 0.25D, this.getZ() * 0.25D, motion.x, motion.y, motion.z);
            }
        }
        float friction = this.motionReduction(water);
        this.setDeltaMovement(motion.scale(friction));
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, this.getGravityVelocity(), 0));
        }
        this.setPos(newX, newY, newZ);
    }

    public static double horizontalMag(Vec3 vec) {
        return vec.x * vec.x + vec.z * vec.z;
    }

    private float updateRotation(float prev, float current) {
        while (current - prev < -180.0F)
            prev -= 360.0F;
        while (current - prev >= 180.0F)
            prev += 360.0F;
        return Mth.lerp(0.2F, prev, current);
    }

    private void doCollision() {
        Vec3 pos = this.position();
        Vec3 to = pos.add(this.getDeltaMovement());
        BlockHitResult raytraceresult = this.level.clip(new ClipContext(pos, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (raytraceresult.getType() != HitResult.Type.MISS) {
            to = raytraceresult.getLocation();
        }

        if (raytraceresult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = raytraceresult.getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(blockpos);
            } else if (blockstate.is(Blocks.END_GATEWAY)) {
                BlockEntity tileentity = this.level.getBlockEntity(blockpos);
                if (tileentity instanceof TheEndGatewayBlockEntity && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                    TheEndGatewayBlockEntity.teleportEntity(this.level, blockpos, blockstate, this, (TheEndGatewayBlockEntity) tileentity);
                }
            } else if (!EventCalls.INSTANCE.projectileHitCall(this, raytraceresult))
                this.onBlockHit(raytraceresult);
        } else {
            EntityHitResult res;
            while ((res = this.getEntityHit(pos, to)) != null && this.isAlive()) {
                this.checkedEntities.add(res.getEntity().getUUID());
                if (!EventCalls.INSTANCE.projectileHitCall(this, res) && !this.attackedEntities.contains(res.getEntity().getUUID()) && this.entityRayTraceHit(res)) {
                    this.attackedEntities.add(res.getEntity().getUUID());
                    if (this.maxPierceAmount() != -1 && this.attackedEntities.size() > this.maxPierceAmount())
                        this.onReachMaxPierce();
                }
            }
        }
    }

    private boolean noGround() {
        return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    private void resetInGround() {
        this.inGround = false;
        this.setDeltaMovement(this.getDeltaMovement().multiply((this.random.nextFloat() * 0.2F), (this.random.nextFloat() * 0.2F), (this.random.nextFloat() * 0.2F)));
        this.ticksInGround = 0;
    }

    @Override
    public void move(MoverType type, Vec3 to) {
        super.move(type, to);
        if (type != MoverType.SELF && this.noGround()) {
            this.resetInGround();
        }
    }

    protected boolean canHit(Entity entity) {
        if (!entity.isSpectator() && entity.isAlive() && entity.isPickable()) {
            if (entity == this.getOwner()) {
                if (!this.canHitShooter() || this.getOwner().isPassengerOfSameVehicle(entity) || this.tickCount < 5)
                    return false;
            }
            return !this.checkedEntities.contains(entity.getUUID());
        }
        return false;
    }

    protected EntityHitResult getEntityHit(Vec3 from, Vec3 to) {
        if (!this.isAlive())
            return null;
        if (this.isPiercing()) {
            if (this.maxPierceAmount() == -1 || this.attackedEntities.size() < this.maxPierceAmount())
                return RayTraceUtils.rayTraceEntities(this, from, to, this::canHit);
            return null;
        }
        if (this.attackedEntities.size() < 1)
            return RayTraceUtils.rayTraceEntities(this, from, to, this::canHit);
        return null;
    }

    protected float getGravityVelocity() {
        return 0.03F;
    }

    protected float motionReduction(boolean inWater) {
        return inWater ? 0.8f : 0.99f;
    }

    protected abstract boolean entityRayTraceHit(EntityHitResult result);

    protected abstract void onBlockHit(BlockHitResult result);

    protected void onReachMaxPierce() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.inGround = compound.getBoolean("InGround");
        if (this.inGround)
            this.setInGround(NbtUtils.readBlockPos(compound.getCompound("GroundPos")));
        if (compound.hasUUID("Shooter"))
            this.entityData.set(shooterUUID, Optional.of(compound.getUUID("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
        ListTag list = compound.getList("AttackedEntities", Tag.TAG_STRING);
        list.forEach(tag -> this.attackedEntities.add(UUID.fromString(tag.getAsString())));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.inGround)
            compound.put("GroundPos", NbtUtils.writeBlockPos(this.groundPos));
        compound.putBoolean("InGround", this.inGround);
        this.entityData.get(shooterUUID).ifPresent(uuid -> compound.putUUID("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
        ListTag list = new ListTag();
        this.attackedEntities.forEach(uuid -> list.add(StringTag.valueOf(uuid.toString())));
        compound.put("AttackedEntities", list);
    }

    @Override
    @Nullable
    public Entity getOwner() {
        if (this.shooter != null && !this.shooter.isRemoved()) {
            return this.shooter;
        }
        this.entityData.get(shooterUUID).ifPresent(uuid -> {
            this.shooter = EntityUtil.findFromUUID(Entity.class, this.level, uuid);
            this.onUpdateOwner();
        });
        return this.shooter;
    }

    public void onUpdateOwner() {
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(shooterUUID).orElse(null);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    /*===== Things below here don't actually get called. They just delegate to the actual implementation. =====*/

    @Override
    protected final void onHitEntity(EntityHitResult result) {
        this.entityRayTraceHit(result);
    }

    @Override
    protected final void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.onBlockHit(result);
    }

    @Override
    public final void setOwner(@Nullable Entity entity) {
        if (entity == null) {
            this.shooter = null;
            this.entityData.set(shooterUUID, Optional.empty());
        } else {
            this.shooter = entity;
            this.entityData.set(shooterUUID, Optional.of(entity.getUUID()));
        }
        this.onUpdateOwner();
    }
}
