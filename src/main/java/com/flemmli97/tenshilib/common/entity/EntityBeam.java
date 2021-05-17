package com.flemmli97.tenshilib.common.entity;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;
import com.flemmli97.tenshilib.common.utils.MathUtils;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class EntityBeam extends Entity implements IBeamEntity {

    private LivingEntity shooter;
    protected int livingTicks;
    protected int coolDown;
    protected RayTraceResult hit;
    protected Vector3d hitVec;

    protected static final DataParameter<Optional<UUID>> shooterUUID = EntityDataManager.createKey(EntityBeam.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    protected final Predicate<Entity> notShooter = (entity) -> entity != EntityBeam.this.getOwner() && EntityPredicates.NOT_SPECTATING.test(entity);

    public EntityBeam(EntityType<? extends EntityBeam> type, World world) {
        super(type, world);
        this.ignoreFrustumCheck = true;
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, World world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public EntityBeam(EntityType<? extends EntityBeam> type, World world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1, shooter.getZ());
        this.shooter = shooter;
        this.dataManager.set(shooterUUID, Optional.of(shooter.getUniqueID()));
        this.setRotation(shooter.rotationYawHead, shooter.rotationPitch);
    }

    public void setRotationTo(double x, double y, double z, float accuracyMod) {
        double dx = x + (this.rand.nextGaussian() * accuracyMod) - this.getX();
        double dy = y + (this.rand.nextGaussian() * accuracyMod) - this.getY();
        double dz = z + (this.rand.nextGaussian() * accuracyMod) - this.getZ();
        this.setRotationToDir(dx, dy, dz, accuracyMod);
    }


    public void setRotationToDir(double x, double y, double z, float accuracyMod) {
        x += this.rand.nextGaussian() * accuracyMod;
        y += this.rand.nextGaussian() * accuracyMod;
        z += this.rand.nextGaussian() * accuracyMod;
        double dis = MathHelper.sqrt(x * x + y * y + z * z);
        this.rotationYaw = (float) (MathHelper.atan2(z, x) * 180.0 / Math.PI) - 90;
        this.rotationPitch = (float) (Math.acos(y / dis) * 180.0 / Math.PI) - 90;
    }

    @Override
    public Vector3d startVec() {
        return this.getPositionVec();
    }

    @Override
    public Vector3d hitVec() {
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
        this.updateYawPitch();
        if (this.hit == null || this.getHitVecFromShooter()) {
            this.hit = this.getHitRay();
            this.hitVec = this.hit.getHitVec();
            if (this.hit.getType() == RayTraceResult.Type.BLOCK) {
                Vector3d dir = this.hitVec.subtract(this.getPositionVec()).normalize();
                this.hitVec = this.hitVec.subtract(dir.scale(this.radius() * 0.3));
            }
        }
        super.tick();
        this.livingTicks++;
        if (this.livingTicks >= this.livingTickMax())
            this.remove();
        if (!this.world.isRemote && this.hit != null && --this.coolDown <= 0 && this.isAlive()) {
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this,
                    new AxisAlignedBB(this.getX(), this.getY(), this.getZ(), this.hitVec.x, this.hitVec.y, this.hitVec.z).grow(1 + this.radius()));
            Vector3d pos = this.getPositionVec();
            for (Entity entity : list) {
                if (entity != this.getOwner() && this.check(entity, pos, this.hitVec)) {
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

    public RayTraceResult getHitRay() {
        return RayTraceUtils.entityRayTrace(this, this.getRange(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE,
                !this.piercing(), true, this.notShooter);
    }

    protected boolean check(Entity e, Vector3d from, Vector3d to) {
        AxisAlignedBB aabb = e.getBoundingBox().grow(this.radius() + 0.3);
        Optional<Vector3d> ray = aabb.rayTrace(from, to);
        if (!ray.isPresent() && !aabb.contains(this.getPositionVec()))
            return false;
        if (this.radius() == 0)
            return true;
        double dist = MathUtils.distTo(e, from, to);
        Vector3d dir = to.subtract(from).normalize().scale(0.1);
        double maxdist = this.radius() + e.getWidth() + 0.3;
        return dist <= maxdist * maxdist && MathUtils.isInFront(e.getPositionVec(), from, dir);
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
    public UUID getOwnerUUID() {
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

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
