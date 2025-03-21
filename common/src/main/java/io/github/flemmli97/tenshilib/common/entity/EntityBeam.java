package io.github.flemmli97.tenshilib.common.entity;

import io.github.flemmli97.tenshilib.api.entity.IBeamEntity;
import io.github.flemmli97.tenshilib.common.utils.OrientedBoundingBox;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class EntityBeam extends Entity implements IBeamEntity {

    protected static final EntityDataAccessor<Optional<UUID>> SHOOTER_UUID = SynchedEntityData.defineId(EntityBeam.class, EntityDataSerializers.OPTIONAL_UUID);

    private Entity shooter;
    protected int livingTicks;
    protected HitResult hit;
    protected Vec3 hitVec;

    protected OrientedBoundingBox hitObb;

    public EntityBeam(EntityType<? extends EntityBeam> type, Level world) {
        super(type, world);
        this.noCulling = true;
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, Level world, double x, double y, double z) {
        this(type, world);
        this.setPos(x, y, z);
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, Level world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1, shooter.getZ());
        this.shooter = shooter;
        this.entityData.set(SHOOTER_UUID, Optional.of(shooter.getUUID()));
        this.setRot(shooter.yHeadRot, shooter.getXRot());
    }

    public void setRotationTo(Entity target, float inaccuracy) {
        Vec3 targetPos = EntityUtil.getStraightProjectileTarget(this.position(), target);
        this.setRotationTo(targetPos.x(), targetPos.y(), targetPos.z(), inaccuracy);
    }

    public void setRotationTo(double x, double y, double z, float inaccuracy) {
        double dx = x - this.getX();
        double dy = y - this.getY();
        double dz = z - this.getZ();
        this.setRotationToDir(dx, dy, dz, inaccuracy);
    }

    public void setRotationToDir(double x, double y, double z, float inaccuracy) {
        x += this.random.nextGaussian() * inaccuracy;
        y += this.random.nextGaussian() * inaccuracy;
        z += this.random.nextGaussian() * inaccuracy;
        double dis = Math.sqrt(x * x + y * y + z * z);
        this.setYRot((float) (Mth.atan2(z, x) * 180.0 / Math.PI) - 90);
        this.setXRot((float) (Math.acos(y / dis) * 180.0 / Math.PI) - 90);
    }

    @Override
    public Vec3 startVec() {
        return this.position();
    }

    @Override
    public Vec3 hitVec() {
        return this.hit != null ? this.hitVec : this.startVec();
    }

    public float getRange() {
        return 32;
    }

    @Override
    public float radius() {
        return 0;
    }

    public boolean piercing() {
        return false;
    }

    @Override
    public int livingTickMax() {
        return 20;
    }

    @Override
    public void updateYawPitch() {
        if (this.getHitVecFromShooter() && this.getOwner() != null) {
            Entity e = this.getOwner();
            this.setXRot(e.getXRot());
            this.setYRot(e.getYRot());
            this.xRotO = e.xRotO;
            this.yRotO = e.yRotO;
            this.setPos(e.getX(), e.getY() + e.getEyeHeight() - 0.10000000149011612D, e.getZ());
        }
    }

    /**
     * post update the projectiles heading and stuff after beeing shot
     */
    public boolean getHitVecFromShooter() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < Math.max(1048, (this.getRange() + 2) * (this.getRange() + 2));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SHOOTER_UUID, Optional.empty());
    }

    public void updateHitDetectBox() {
        double dist = this.hitVec != null ? this.hitVec.subtract(this.position()).length() : 0;
        this.hitObb = new OrientedBoundingBox(OrientedBoundingBox.baseBox(this.radius() * 2, this.radius() * 2, dist),
                this.getYRot(), -this.getXRot(), this.position());
    }

    @Override
    public void tick() {
        this.updateYawPitch();
        if (this.hit == null || this.getHitVecFromShooter()) {
            this.hit = this.getHitRay();
            this.hitVec = this.hit.getLocation();
            if (this.hit.getType() == HitResult.Type.BLOCK) {
                Vec3 dir = this.hitVec.subtract(this.position()).normalize();
                this.hitVec = this.hitVec.subtract(dir.scale(this.radius() * 0.3));
            }
            this.updateHitDetectBox();
        }
        super.tick();
        this.livingTicks++;
        if (!this.level.isClientSide) {
            if (this.livingTicks > this.livingTickMax()) {
                this.remove(RemovalReason.KILLED);
                return;
            }
            if (this.hit != null && this.canStartDamage() && this.isAlive()) {
                List<Entity> list = this.level.getEntities(this,
                        new AABB(this.getX(), this.getY(), this.getZ(), this.hitVec.x, this.hitVec.y, this.hitVec.z).inflate(1 + this.radius()));
                Predicate<AABB> collisionCheck = aabb -> {
                    if (this.radius() == 0) {
                        Optional<Vec3> ray = aabb.clip(this.position(), this.hitVec);
                        return ray.isPresent() || aabb.contains(this.position());
                    }
                    return this.hitObb.intersects(aabb);
                };
                for (Entity entity : list) {
                    if (!entity.equals(this.getOwner()) && this.canHitEntity(entity) && this.check(entity, collisionCheck)) {
                        EntityHitResult raytraceresult = new EntityHitResult(entity);
                        if (!EventCalls.INSTANCE.beamHitCall(this, raytraceresult)) {
                            this.onImpact(raytraceresult);
                            if (!this.piercing())
                                return;
                        }
                    }
                }
            }
        }
    }

    protected boolean canHitEntity(Entity target) {
        if (target.isSpectator() || !target.isAlive() || !target.isPickable()) {
            return false;
        }
        Entity entity = this.getOwner();
        if (entity == null)
            return true;
        return target != entity && !EntityUtil.isSameMultipart(target, entity) && !entity.isPassengerOfSameVehicle(target);
    }

    public HitResult getHitRay() {
        return RayTraceUtils.entityRayTrace(this, this.getRange(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                !this.piercing(), true, this::canHitEntity);
    }

    protected boolean check(Entity e, Predicate<AABB> intersects) {
        if (e.isSpectator() || !e.isAlive() || !e.isPickable())
            return false;
        AABB aabb = e.getBoundingBox();
        return intersects.test(aabb);
    }

    public abstract void onImpact(EntityHitResult result);

    public int livingTicks() {
        return this.livingTicks;
    }

    public boolean canStartDamage() {
        return (this.livingTicks - 1) % 20 == 0;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Shooter"))
            this.entityData.set(SHOOTER_UUID, Optional.of(compound.getUUID("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        this.entityData.get(SHOOTER_UUID).ifPresent(uuid -> compound.putUUID("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
    }

    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(SHOOTER_UUID).orElse(null);
    }

    @Override
    @Nullable
    public Entity getOwner() {
        if (this.shooter != null && !this.shooter.isRemoved()) {
            return this.shooter;
        }
        this.entityData.get(SHOOTER_UUID).ifPresent(uuid -> this.shooter = EntityUtil.findFromUUID(Entity.class, this.level, uuid));
        return this.shooter;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
