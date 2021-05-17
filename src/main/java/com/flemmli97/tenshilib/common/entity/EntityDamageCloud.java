package com.flemmli97.tenshilib.common.entity;

import com.flemmli97.tenshilib.api.entity.IOwnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityDamageCloud extends Entity implements IOwnable<LivingEntity> {

    private LivingEntity shooter;

    protected int livingTicks;
    private int attackedEntities = 0;

    protected static final DataParameter<Optional<UUID>> shooterUUID = EntityDataManager.createKey(EntityDamageCloud.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Float> radius = EntityDataManager.createKey(EntityDamageCloud.class, DataSerializers.FLOAT);

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, World world) {
        super(type, world);
    }

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, World world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public EntityDamageCloud(EntityType<? extends EntityDamageCloud> type, World world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY(), shooter.getZ());
        this.shooter = shooter;
        this.dataManager.set(shooterUUID, Optional.of(shooter.getUniqueID()));
        this.setRotation(shooter.rotationYaw, shooter.rotationPitch);
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
    protected void registerData() {
        this.dataManager.register(shooterUUID, Optional.empty());
        this.dataManager.register(radius, 0f);
    }

    public float getRadius() {
        return this.dataManager.get(radius);
    }

    public void setRadius(float val) {
        this.dataManager.set(radius, val);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 4.0D;
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
        if (!this.world.isRemote) {
            if (this.livingTicks > this.livingTickMax())
                this.remove();
            float radius = this.getRadius();
            if (radius < this.maxRadius()) {
                this.setRadius(radius + this.radiusIncrease());
            }
            if (this.canStartDamage() && this.isAlive()) {
                List<LivingEntity> targets = this.world.getEntitiesWithinAABB(LivingEntity.class, this.damageBoundingBox(), this::canHit);
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
        return this.getOwner() == null || (!this.getOwner().isRidingSameEntity(entity) && ((this.canHitShooter() && this.ticksExisted > 2) || entity != this.getOwner()));
    }

    protected abstract boolean damageEntity(LivingEntity target);

    protected void onMaxEntities() {
        this.remove();
    }

    protected AxisAlignedBB damageBoundingBox() {
        float radius = this.getRadius();
        return this.getBoundingBox().grow(radius, 0.3, radius);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("Shooter"))
            this.dataManager.set(shooterUUID, Optional.of(compound.getUniqueId("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
        this.attackedEntities = compound.getInt("AttackedEntities");
        this.dataManager.set(radius, compound.getFloat("Radius"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        this.dataManager.get(shooterUUID).ifPresent(uuid -> compound.putUniqueId("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
        compound.putInt("AttackedEntities", this.attackedEntities);
        compound.putFloat("Radius", this.getRadius());
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        if (this.shooter == null) {
            this.dataManager.get(shooterUUID).ifPresent(uuid -> this.shooter = EntityUtil.findFromUUID(LivingEntity.class, this.world, uuid));
        }
        return this.shooter;
    }

    @Override
    public UUID getOwnerUUID() {
        return this.dataManager.get(shooterUUID).orElse(null);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
