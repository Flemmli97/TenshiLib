package com.flemmli97.tenshilib.common.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityProjectile extends Entity implements IProjectile{

	private EntityLivingBase shooter;

	protected boolean inGround;
	protected int ticksInGround, livingTicks;
    public List<UUID> attackedEntities = Lists.newArrayList();
    
    private Block ground;
    private BlockPos groundPos;

    protected static final DataParameter<String> shooterUUID = EntityDataManager.createKey(EntityProjectile.class, DataSerializers.STRING);

	public EntityProjectile(World world) {
		super(world);
		this.setSize(0.25F, 0.25F);
	}

	public EntityProjectile(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }
	
	public EntityProjectile(World world, EntityLivingBase shooter)
    {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ);
        this.shooter = shooter;
        this.dataManager.set(shooterUUID, shooter.getUniqueID().toString());
        this.setRotation(shooter.rotationYaw, shooter.rotationPitch);
    }
    
    public boolean isPiercing()
    {
    	return false;
    }
    
    public int maxPierceAmount()
    {
    	return -1;
    }
    
	/**
	 * Doesnt work properly yet
	 */
    public float radius()
    {
    	return 0;
    }
    
	public int livingTicks()
	{
		return this.livingTicks;
	}
	
	public int livingTickMax()
	{
		return 6000;
	}
	
	public boolean canHitShooter()
	{
		return false;
	}
	
	@Override
	protected void entityInit() {
		this.dataManager.register(shooterUUID, "");
	}

	@Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0))
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }
    
	public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
    {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot(f, f1, f2, velocity, inaccuracy);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround)
        {
            this.motionY += entityThrower.motionY;
        }
    }

    public void shootAtPosition(double x, double y, double z, float velocity, float inaccuracy)
    {
		Vec3d dir = new Vec3d (x-this.posX, y - this.posY, z-this.posZ).scale(1/velocity);
		this.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
    }
    
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / f;
        y = y / f;
        z = z / f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
        x = x * velocity;
        y = y * velocity;
        z = z * velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.ticksInGround = 0;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z)
    {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
            this.rotationPitch = (float)(MathHelper.atan2(y, f) * (180D / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }
    }
	
	public void setInGround(BlockPos pos) {
		IBlockState state = this.world.getBlockState(pos);
		if(!state.getMaterial().isSolid())
		{
			this.inGround=false;
			return;
		}
		this.inGround=true;
		this.groundPos=pos;
		this.ground=state.getBlock();
	}
	
	@Override
    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();  
        this.livingTicks++;
        if(this.livingTicks>this.livingTickMax())
        	this.setDead();
        if (this.inGround)
		{
	        if (this.world.getBlockState(this.groundPos).getBlock() == this.ground)
	        {
	            ++this.ticksInGround;
	
	            if (this.ticksInGround == 1200)
	            {
	                this.setDead();
	            }
	            return;
	        }
	        this.inGround = false;
	        this.motionX *= (this.rand.nextFloat() * 0.2F);
	        this.motionY *= (this.rand.nextFloat() * 0.2F);
	        this.motionZ *= (this.rand.nextFloat() * 0.2F);
	        this.ticksInGround = 0;
	    }
        
        if(!this.world.isRemote)
        	this.doCollision();

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
        for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float motionReduction = this.motionReduction();
        if (this.isInWater())
        {
            for (int j = 0; j < 4; ++j)
            {
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
            }
            motionReduction = 0.8F;
        }
        this.motionX *= motionReduction;
        this.motionY *= motionReduction;
        this.motionZ *= motionReduction;
        if (!this.hasNoGravity())
        {
            this.motionY -= this.getGravityVelocity();
        }
        this.setPosition(this.posX, this.posY, this.posZ);
    }

	private void doCollision()
	{
        Vec3d vec3d = this.getPositionVector();
        Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        //Vec3d aabbRadius = new Vec3d(this.motionX, this.motionY, this.motionZ).normalize().scale(this.radius());
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1);

        if (raytraceresult != null)
        {
            vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
        }

        Entity entity = null;
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
        boolean flag=false;
        double d0 = 0.0D;
        for (int i = 0; i < list.size(); ++i)
        {
            Entity entity1 = list.get(i);
            if (entity1.canBeCollidedWith() && (this.getShooter()==null||this.canHitShooter() && (entity1!=this.getShooter() || this.ticksExisted>2) || entity1!=this.getShooter()))
            {
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(/*this.radius()+*/0.30000001192092896D);
                RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
                if (raytraceresult1 != null || axisalignedbb.contains(vec3d1))
                {
	                if(this.isPiercing())
	                {
	                	raytraceresult1 = new RayTraceResult(entity1);
	                	if (!this.attackedEntities.contains(entity1.getUniqueID()) && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult1))
	                    {
	                		flag=true;
	                        this.onImpact(raytraceresult1);
	                        this.attackedEntities.add(entity1.getUniqueID());
	                    }
	                }
	                else
	                {	
	                    double d1 = raytraceresult1!=null?vec3d.squareDistanceTo(raytraceresult1.hitVec):0;
	                    if (d1 < d0 || d0 == 0.0D)
	                    {
	                        entity = entity1;
	                        d0 = d1;
	                    }
	                }
                }
            }
        }
        if (entity != null)
        {
            raytraceresult = new RayTraceResult(entity);
        }

        if (raytraceresult != null && !flag)
        {
            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL)
            {
                this.setPortal(raytraceresult.getBlockPos());
            }
            else if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
            {
                this.onImpact(raytraceresult);
            }
        }
	}
	
	protected float getGravityVelocity()
    {
        return 0.03F;
    }
	
	protected float motionReduction()
	{
		return 0.99f;
	}
	
    protected abstract void onImpact(RayTraceResult result);

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.inGround=compound.getBoolean("InGround");
		if(this.inGround)
		{
			int[] arr = compound.getIntArray("GroundPos");
			this.setInGround(new BlockPos(arr[0], arr[1], arr[2]));
		}
		if(compound.hasKey("Shooter"))
			this.dataManager.set(shooterUUID, compound.getString("Shooter"));
		this.shooter=this.getShooter();
		this.livingTicks=compound.getInteger("LivingTicks");
		NBTTagList list = compound.getTagList("AttackedEntities", Constants.NBT.TAG_STRING);
		list.forEach(tag->this.attackedEntities.add(UUID.fromString(((NBTTagString)tag).getString())));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		if(this.inGround)
		{
			compound.setIntArray("GroundPos", new int[] {this.groundPos.getX(), this.groundPos.getY(), this.groundPos.getZ()});
		}
        compound.setBoolean("InGround", this.inGround);
    	compound.setString("Shooter", this.dataManager.get(shooterUUID));
        compound.setInteger("LivingTicks", this.livingTicks);
        NBTTagList list = new NBTTagList();
        this.attackedEntities.forEach(uuid->list.appendTag(new NBTTagString(uuid.toString())));
        compound.setTag("AttackedEntities", list);
	}

    @Nullable
    public EntityLivingBase getShooter()
    {
    	if(this.shooter==null && !this.dataManager.get(shooterUUID).isEmpty())
        {
        	this.shooter=EntityUtil.findFromUUID(EntityLivingBase.class, this.world, UUID.fromString(this.dataManager.get(shooterUUID)));
        }
        return this.shooter;
    }
}
