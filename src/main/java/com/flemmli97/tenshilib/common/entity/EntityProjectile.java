package com.flemmli97.tenshilib.common.entity;

import com.flemmli97.tenshilib.api.entity.IOwnable;
import com.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityProjectile extends Entity implements IOwnable<LivingEntity> {

    private LivingEntity shooter;

    protected boolean inGround;
    protected int ticksInGround, livingTicks;
    public final List<UUID> attackedEntities = new ArrayList<>();
    public final List<UUID> checkedEntities = new ArrayList<>();

    private BlockState ground;
    private BlockPos groundPos;

    protected static final DataParameter<Optional<UUID>> shooterUUID = EntityDataManager.createKey(EntityProjectile.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    public EntityProjectile(EntityType<? extends EntityProjectile> type, World world) {
        super(type, world);
    }

    public EntityProjectile(EntityType<? extends EntityProjectile> type, World world, double x, double y, double z) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public EntityProjectile(EntityType<? extends EntityProjectile> type, World world, LivingEntity shooter) {
        this(type, world, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1, shooter.getZ());
        this.shooter = shooter;
        this.dataManager.set(shooterUUID, Optional.of(shooter.getUniqueID()));
        this.setRotation(shooter.rotationYaw, shooter.rotationPitch);
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
    protected void registerData() {
        this.dataManager.register(shooterUUID, Optional.empty());
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

    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot(f, f1, f2, velocity, inaccuracy);
        Vector3d throwerMotion = entityThrower.getMotion();
        this.setMotion(this.getMotion().add(throwerMotion.x, entityThrower.isOnGround() ? 0.0D : throwerMotion.y, throwerMotion.z));
        this.getMotion().add(throwerMotion.x, 0, throwerMotion.z);
    }

    /**
     * Shoots directly at the given position
     */
    public void shootAtPosition(double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d dir = new Vector3d(x - this.getX(), y - this.getY(), z - this.getZ());
        this.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }

    /**
     * Shoot at the given entity. Unlike #shootAtPosition this doesnt shoot directly where the entity is but rather through it (like arrows)
     *
     * @param yOffsetModifier Modifies the offset of the y motion based on distance to target. Vanilla arrows use 0.2
     */
    public void shootAtEntity(Entity target, float velocity, float inaccuracy, float yOffsetModifier) {
        Vector3d dir = (new Vector3d(target.getX() - this.getX(), target.getBodyY(0.33) - this.getY(), target.getZ() - this.getZ()));
        double l = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.shoot(dir.x, dir.y + l * yOffsetModifier, dir.z, velocity, inaccuracy);
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0075F * inaccuracy, this.rand.nextGaussian() * 0.0075F * inaccuracy, this.rand.nextGaussian() * 0.0075F * inaccuracy).scale(velocity);
        this.setMotion(vector3d);
        float f = MathHelper.sqrt(horizontalMag(vector3d));
        this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, f) * (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.ticksInGround = 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.setMotion(x, y, z);
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = (float) (MathHelper.atan2(y, f) * (180F / (float) Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180F / (float) Math.PI));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.getX(), this.getY(), this.getZ(), this.rotationYaw, this.rotationPitch);
        }
    }

    public void setInGround(BlockPos pos) {
        BlockState state = this.world.getBlockState(pos);
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
        if (this.livingTicks > this.livingTickMax())
            this.remove();
        Vector3d motion = this.getMotion();
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(horizontalMag(motion));
            this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (180F / Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (180F / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        BlockState inState = this.world.getBlockState(this.getBlockPos());
        if (this.inGround) {
            if (inState != this.ground && this.noGround())
                this.resetInGround();
            else if (!this.world.isRemote) {
                ++this.ticksInGround;
                if (this.ticksInGround == 1200)
                    this.remove();
            }
            return;
        }

        if (!this.world.isRemote)
            this.doCollision();

        double newX = this.getX() + motion.x;
        double newY = this.getY() + motion.y;
        double newZ = this.getZ() + motion.z;

        float f = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = this.updateRotation(this.prevRotationYaw, (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI)));
        this.rotationPitch = this.updateRotation(this.prevRotationPitch, (float) (MathHelper.atan2(motion.y, f) * (double) (180F / (float) Math.PI)));

        float friction;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.world.addParticle(ParticleTypes.BUBBLE, this.getX() * 0.25D, this.getY() * 0.25D, this.getZ() * 0.25D, motion.x, motion.y, motion.z);
            }
            friction = 0.8F;
        } else {
            friction = this.motionReduction();
        }

        this.setMotion(motion.scale(friction));
        if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().subtract(0, this.getGravityVelocity(), 0));
        }
        this.setPosition(newX, newY, newZ);
    }

    private float updateRotation(float prev, float current) {
        while (current - prev < -180.0F)
            prev -= 360.0F;
        while (current - prev >= 180.0F)
            prev += 360.0F;
        return MathHelper.lerp(0.2F, prev, current);
    }

    private void doCollision() {
        Vector3d pos = this.getPositionVec();
        Vector3d to = pos.add(this.getMotion());
        //Vector3d aabbRadius = new Vector3d(this.motionX, this.motionY, this.motionZ).normalize().scale(this.radius());
        BlockRayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(pos, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
        if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            to = raytraceresult.getHitVec();
        }

        if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = raytraceresult.getPos();
            BlockState blockstate = this.world.getBlockState(blockpos);
            if (blockstate.isIn(Blocks.NETHER_PORTAL)) {
                this.setPortal(blockpos);
            } else if (blockstate.isIn(Blocks.END_GATEWAY)) {
                TileEntity tileentity = this.world.getTileEntity(blockpos);
                if (tileentity instanceof EndGatewayTileEntity && EndGatewayTileEntity.method_30276(this)) {
                    ((EndGatewayTileEntity) tileentity).teleportEntity(this);
                }
            } else if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
                this.onBlockHit(raytraceresult);
        } else {
            EntityRayTraceResult res;
            while ((res = this.getEntityHit(pos, to)) != null && this.isAlive()) {
                this.checkedEntities.add(res.getEntity().getUniqueID());
                if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, res) && !this.attackedEntities.contains(res.getEntity().getUniqueID()) && this.onEntityHit(res)) {
                    this.attackedEntities.add(res.getEntity().getUniqueID());
                    if (this.maxPierceAmount() != -1 && this.attackedEntities.size() > this.maxPierceAmount())
                        this.onReachMaxPierce();
                }
            }
        }
    }

    private boolean noGround() {
        return this.inGround && this.world.isSpaceEmpty((new AxisAlignedBB(this.getPositionVec(), this.getPositionVec())).grow(0.06D));
    }

    private void resetInGround() {
        this.inGround = false;
        this.setMotion(this.getMotion().mul((this.rand.nextFloat() * 0.2F), (this.rand.nextFloat() * 0.2F), (this.rand.nextFloat() * 0.2F)));
        this.ticksInGround = 0;
    }

    @Override
    public void move(MoverType type, Vector3d to) {
        super.move(type, to);
        if (type != MoverType.SELF && this.noGround()) {
            this.resetInGround();
        }
    }

    protected boolean canHit(Entity entity) {
        if (!entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith()) {
            if (this.getOwner() == null || (!this.getOwner().isRidingSameEntity(entity) && ((this.canHitShooter() && this.ticksExisted > 5) || entity != this.getOwner())))
                return !this.checkedEntities.contains(entity.getUniqueID());
        }
        return false;
    }

    protected EntityRayTraceResult getEntityHit(Vector3d from, Vector3d to) {
        if (!this.isAlive())
            return null;
        if (this.isPiercing()) {
            if (this.maxPierceAmount() == -1 || this.attackedEntities.size() < this.maxPierceAmount())
                return RayTraceUtils.projectileHit(this.world, this, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), this::canHit, 0);
            //return RayTraceUtils.projectileRayTrace(this.world, this, from, to, this.getBoundingBox().expand(this.getMotion()).grow(this.radius() + 1.0D), this::canHit, this.radius());
            return null;
        }
        if (this.attackedEntities.size() < 1)
            return RayTraceUtils.projectileHit(this.world, this, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), this::canHit, 0);
        //return RayTraceUtils.projectileRayTrace(this.world, this, from, to, this.getBoundingBox().expand(this.getMotion()).grow(this.radius() + 1.0D), this::canHit,  this.radius());
        return null;
    }

    protected float getGravityVelocity() {
        return 0.03F;
    }

    protected float motionReduction() {
        return 0.99f;
    }

    protected abstract boolean onEntityHit(EntityRayTraceResult result);

    protected abstract void onBlockHit(BlockRayTraceResult result);

    protected void onReachMaxPierce() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.inGround = compound.getBoolean("InGround");
        if (this.inGround)
            this.setInGround(NBTUtil.readBlockPos(compound.getCompound("GroundPos")));
        if (compound.hasUniqueId("Shooter"))
            this.dataManager.set(shooterUUID, Optional.of(compound.getUniqueId("Shooter")));
        this.shooter = this.getOwner();
        this.livingTicks = compound.getInt("LivingTicks");
        ListNBT list = compound.getList("AttackedEntities", Constants.NBT.TAG_STRING);
        list.forEach(tag -> this.attackedEntities.add(UUID.fromString(tag.getString())));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (this.inGround)
            compound.put("GroundPos", NBTUtil.writeBlockPos(this.groundPos));
        compound.putBoolean("InGround", this.inGround);
        this.dataManager.get(shooterUUID).ifPresent(uuid -> compound.putUniqueId("Shooter", uuid));
        compound.putInt("LivingTicks", this.livingTicks);
        ListNBT list = new ListNBT();
        this.attackedEntities.forEach(uuid -> list.add(StringNBT.of(uuid.toString())));
        compound.put("AttackedEntities", list);
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
