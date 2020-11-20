package com.flemmli97.tenshilib.common.entity;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;
import com.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class EntityBeam extends Entity implements IBeamEntity {

    private LivingEntity shooter;
    protected int livingTicks;
    private int coolDown;
    private RayTraceResult hit;

    protected static final DataParameter<Optional<UUID>> shooterUUID = EntityDataManager.createKey(EntityBeam.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    private final Predicate<Entity> notShooter = (entity) -> entity != EntityBeam.this.getOwner() && EntityPredicates.NOT_SPECTATING.test(entity);

    public EntityBeam(EntityType<? extends EntityBeam> type, World world) {
        super(type, world);
        this.ignoreFrustumCheck = true;
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, World world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, World world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.10000000149011612D, shooter.getZ());
        this.shooter = shooter;
        this.dataManager.set(shooterUUID, Optional.of(shooter.getUniqueID()));
        this.setRotation(shooter.rotationYawHead, shooter.rotationPitch);
    }

    public void setRotationTo(double x, double y, double z, float accuracyMod) {
        double dx = x + (this.rand.nextGaussian() * accuracyMod) - this.getX();
        double dy = y + (this.rand.nextGaussian() * accuracyMod) - this.getY();
        double dz = z + (this.rand.nextGaussian() * accuracyMod) - this.getZ();
        double dis = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        this.rotationYaw = (float) (MathHelper.atan2(dz, dx) * 180.0 / Math.PI) - 90;
        this.rotationPitch = (float) (Math.acos(dy / dis) * 180.0 / Math.PI) - 90;
    }

    @Override
    public Vector3d startVec() {
        return this.getPositionVec();
    }

    @Override
    public Vector3d hitVec() {
        return this.hit != null ? this.hit.getHitVec() : this.startVec();
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
            LivingEntity e = this.getOwner();
            this.rotationPitch = e.rotationPitch;
            this.rotationYaw = e.rotationYaw;
            this.prevRotationPitch = e.prevRotationPitch;
            this.prevRotationYaw = e.prevRotationYaw;
            this.setPosition(e.getX(), e.getY() + e.getEyeHeight() - 0.10000000149011612D, e.getZ());
        }
    }

    /**
     * post update the projectiles heading and stuff after beeing shot
     */
    public boolean getHitVecFromShooter() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < Math.max(1048, (this.getRange() + 2) * (this.getRange() + 2));
    }

    @Override
    protected void registerData() {
        this.dataManager.register(shooterUUID, Optional.empty());
    }

    @Override
    public void tick() {
        if (this.getOwner() != null) {
            if (this.hit == null || this.getHitVecFromShooter())
                this.hit = RayTraceUtils.entityRayTrace(this.getHitVecFromShooter() ? this.getOwner() : this, this.getRange(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE,
                        !this.piercing(), this.notShooter);
        }
        this.updateYawPitch();
        super.tick();
        this.livingTicks++;
        if (this.livingTicks >= this.livingTickMax())
            this.remove();
        if (!this.world.isRemote && this.hit != null && --this.coolDown <= 0) {
            Vector3d offSetPosVec = this.getPositionVec();//.add(this.getLookVec().scale(this.radius()));
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this,
                    new AxisAlignedBB(this.getX(), this.getY(), this.getZ(), this.hit.getHitVec().x, this.hit.getHitVec().y, this.hit.getHitVec().z).grow(1));
            for (Entity entity : list) {
                if (entity.canBeCollidedWith() && entity != this.getOwner()) {
                    AxisAlignedBB axisalignedbb = entity.getBoundingBox().grow(this.radius() + 0.30000001192092896D);
                    Optional<Vector3d> res = axisalignedbb.rayTrace(offSetPosVec, this.hit.getHitVec());
                    if (res.isPresent()) {
                        EntityRayTraceResult raytraceresult = new EntityRayTraceResult(entity);
                        if (!ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
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

    public abstract void onImpact(EntityRayTraceResult result);

    public int livingTicks() {
        return this.livingTicks;
    }

    public int attackCooldown() {
        return 20;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("Shooter"))
            this.dataManager.set(shooterUUID, Optional.of(compound.getUniqueId("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        this.dataManager.get(shooterUUID).ifPresent(uuid -> compound.putUniqueId("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
    }

    @Override
    public UUID ownerUUID() {
        return this.dataManager.get(shooterUUID).orElse(null);
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
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
