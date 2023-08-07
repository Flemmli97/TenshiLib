package io.github.flemmli97.tenshilib.common.entity;

import io.github.flemmli97.tenshilib.api.entity.IBeamEntity;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class EntityBeam extends Entity implements IBeamEntity {

    private LivingEntity shooter;
    protected int livingTicks;
    protected int coolDown;
    protected HitResult hit;
    protected Vec3 hitVec;

    protected static final EntityDataAccessor<Optional<UUID>> shooterUUID = SynchedEntityData.defineId(EntityBeam.class, EntityDataSerializers.OPTIONAL_UUID);

    protected final Predicate<Entity> notShooter = (entity) -> entity != EntityBeam.this.getOwner() && EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable();

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
        this.entityData.set(shooterUUID, Optional.of(shooter.getUUID()));
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

    /**
     * Doesnt work properly yet
     */
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
        this.entityData.define(shooterUUID, Optional.empty());
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
        }
        super.tick();
        this.livingTicks++;
        if (!this.level.isClientSide) {
            if (this.livingTicks >= this.livingTickMax())
                this.remove(RemovalReason.KILLED);
            if (this.hit != null && --this.coolDown <= 0 && this.isAlive()) {
                List<Entity> list = this.level.getEntities(this,
                        new AABB(this.getX(), this.getY(), this.getZ(), this.hitVec.x, this.hitVec.y, this.hitVec.z).inflate(1 + this.radius()));
                Vec3 pos = this.position();
                for (Entity entity : list) {
                    if (!entity.equals(this.getOwner()) && !EntityUtil.isSameMultipart(entity, this.getOwner()) && this.check(entity, pos, this.hitVec)) {
                        EntityHitResult raytraceresult = new EntityHitResult(entity);
                        if (!EventCalls.INSTANCE.beamHitCall(this, raytraceresult)) {
                            this.onImpact(raytraceresult);
                            this.coolDown = this.attackCooldown();
                            if (!this.piercing())
                                return;
                        }
                    }
                }
            }
        }
    }

    public HitResult getHitRay() {
        return RayTraceUtils.entityRayTrace(this, this.getRange(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                !this.piercing(), true, this.notShooter);
    }

    protected boolean check(Entity e, Vec3 from, Vec3 to) {
        if (e.isSpectator() || !e.isAlive() || !e.isPickable())
            return false;
        AABB aabb = e.getBoundingBox().inflate(this.radius() + 0.3);
        Optional<Vec3> ray = aabb.clip(from, to);
        if (ray.isEmpty() && !aabb.contains(this.position()))
            return false;
        if (this.radius() == 0)
            return true;
        double dist = MathUtils.distTo(e, from, to);
        Vec3 dir = to.subtract(from).normalize().scale(0.1);
        double maxdist = this.radius() + e.getBbWidth() + 0.3;
        return dist <= maxdist * maxdist && MathUtils.isInFront(e.position(), from, dir);
    }

    public abstract void onImpact(EntityHitResult result);

    public int livingTicks() {
        return this.livingTicks;
    }

    public int attackCooldown() {
        return 20;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Shooter"))
            this.entityData.set(shooterUUID, Optional.of(compound.getUUID("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        this.entityData.get(shooterUUID).ifPresent(uuid -> compound.putUUID("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
    }

    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(shooterUUID).orElse(null);
    }

    @Override
    public LivingEntity getOwner() {
        if (this.shooter != null && !this.shooter.isRemoved()) {
            return this.shooter;
        }
        this.entityData.get(shooterUUID).ifPresent(uuid -> this.shooter = EntityUtil.findFromUUID(LivingEntity.class, this.level, uuid));
        return this.shooter;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
