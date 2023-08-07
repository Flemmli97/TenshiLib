package io.github.flemmli97.tenshilib.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityDamageCloud extends Entity implements OwnableEntity {

    private LivingEntity shooter;

    protected int livingTicks;
    private int attackedEntities = 0;

    protected static final EntityDataAccessor<Optional<UUID>> shooterUUID = SynchedEntityData.defineId(EntityDamageCloud.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> radius = SynchedEntityData.defineId(EntityDamageCloud.class, EntityDataSerializers.FLOAT);

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, Level world) {
        super(type, world);
    }

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, Level world, double x, double y, double z) {
        this(type, world);
        this.setPos(x, y, z);
    }

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, Level world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY(), shooter.getZ());
        this.shooter = shooter;
        this.entityData.set(shooterUUID, Optional.of(shooter.getUUID()));
        this.setRot(shooter.getYRot(), shooter.getXRot());
    }

    public int maxHitCount() {
        return -1;
    }

    public float radiusIncrease() {
        return 0;
    }

    public double maxRadius() {
        return -1;
    }

    public int livingTicks() {
        return this.livingTicks;
    }

    public int livingTickMax() {
        return 200;
    }

    public boolean canHitShooter() {
        return false;
    }

    public boolean canStartDamage() {
        return this.livingTicks % 5 == 0;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(shooterUUID, Optional.empty());
        this.entityData.define(radius, 0f);
    }

    public float getRadius() {
        return this.entityData.get(radius);
    }

    public void setRadius(float val) {
        this.entityData.set(radius, val);
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

    @Override
    public void tick() {
        super.tick();
        this.livingTicks++;
        if (!this.level.isClientSide) {
            if (this.livingTicks > this.livingTickMax())
                this.remove(RemovalReason.KILLED);
            float radius = this.getRadius();
            if (radius < this.maxRadius()) {
                this.setRadius(radius + this.radiusIncrease());
            }
            if (this.canStartDamage() && this.isAlive()) {
                List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, this.damageBoundingBox(), this::canHit);
                for (LivingEntity living : targets) {
                    if (this.maxHitCount() == -1 || this.attackedEntities < this.maxHitCount()) {
                        if (this.damageEntity(living))
                            this.attackedEntities++;
                    }
                    if (this.maxHitCount() != -1 && this.attackedEntities >= this.maxHitCount()) {
                        this.onMaxEntities();
                        break;
                    }
                }
            }
        }
    }

    protected boolean canHit(LivingEntity entity) {
        return this.getOwner() == null || (!this.getOwner().isPassengerOfSameVehicle(entity) && ((this.canHitShooter() && this.tickCount > 2) || !entity.equals(this.getOwner())));
    }

    protected abstract boolean damageEntity(LivingEntity target);

    protected void onMaxEntities() {
        this.remove(RemovalReason.KILLED);
    }

    protected AABB damageBoundingBox() {
        float radius = this.getRadius();
        return this.getBoundingBox().inflate(radius, 0.3, radius);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Shooter"))
            this.entityData.set(shooterUUID, Optional.of(compound.getUUID("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
        this.attackedEntities = compound.getInt("AttackedEntities");
        this.entityData.set(radius, compound.getFloat("Radius"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        this.entityData.get(shooterUUID).ifPresent(uuid -> compound.putUUID("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
        compound.putInt("AttackedEntities", this.attackedEntities);
        compound.putFloat("Radius", this.getRadius());
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
    public UUID getOwnerUUID() {
        return this.entityData.get(shooterUUID).orElse(null);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
