package com.flemmli97.tenshilib.common.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;
import com.flemmli97.tenshilib.common.world.RayTraceUtils;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityBeam extends Entity implements IBeamEntity{

	private EntityLivingBase shooter;
	protected int livingTicks;
	private int coolDown;
    private RayTraceResult hit;
   
    protected static final DataParameter<String> shooterUUID = EntityDataManager.createKey(EntityBeam.class, DataSerializers.STRING);

    private final Predicate<Entity> notShooter = new Predicate<Entity>() {
		@Override
		public boolean apply(Entity t) {
			return (EntityBeam.this.getShooter()==null || t!=EntityBeam.this.getShooter()) && EntitySelectors.NOT_SPECTATING.apply(t);
		}};
    
	public EntityBeam(World world) {
		super(world);
		this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck=true;
	}

	public EntityBeam(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }
	
	public EntityBeam(World world, EntityLivingBase shooter)
    {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ);
        this.shooter = shooter;
		this.dataManager.set(shooterUUID, shooter.getUniqueID().toString());
        this.setRotation(shooter.rotationYaw, shooter.rotationPitch);   
    }

	@Override
	public Vec3d startVec() {
		return this.getPositionVector();
	}

	@Override
	public Vec3d hitVec() {
		return this.hit!=null?this.hit.hitVec:this.startVec();
	}
	
	public float getRange()
	{
		return 32;
	}
	
	/**
	 * Doesnt work properly yet
	 */
	public float radius()
	{
		return 0;
	}

	public boolean piercing()
	{
		return false;
	}
	
	public int livingTickMax()
	{
		return 20;
	}
	
	public void updateYawPitch() {
		if(this.getHitVecFromShooter() && this.getShooter()!=null)
		{
			EntityLivingBase e = this.getShooter();
			this.rotationPitch=e.rotationPitch;
			this.rotationYaw=e.rotationYaw;
			this.prevRotationPitch=e.prevRotationPitch;
			this.prevRotationYaw=e.prevRotationYaw;
			this.setPosition(e.posX, e.posY+e.getEyeHeight()- 0.10000000149011612D, e.posZ);
		}
	}
	
    public boolean getHitVecFromShooter() {
		return false;
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance<Math.max(1048, (this.getRange()+2)*(this.getRange()+2));
    }
	
	@Override
	protected void entityInit() {
		this.dataManager.register(shooterUUID, "");
	}

	@Override
    public void onUpdate()
    {
		if(this.getShooter()!=null)
		{
			if(this.hit==null || this.getHitVecFromShooter())
		        this.hit=RayTraceUtils.entityRayTrace(this.getHitVecFromShooter()?this.getShooter():this, this.getRange(), false, true, false, !this.piercing(), notShooter);
		}
		this.updateYawPitch();
		super.onUpdate();
		this.livingTicks++;
        if(this.livingTicks>=this.livingTickMax())
        	this.setDead();
        if(this.hit!=null && --this.coolDown<=0)
		{
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX,this.posY,this.posZ,this.hit.hitVec.x,this.hit.hitVec.y,this.hit.hitVec.z).grow(1));
			for(int i = 0; i < list.size(); i++)
			{
				Entity entity = list.get(i);
				if (entity.canBeCollidedWith() && (this.getShooter()==null || entity!=this.getShooter()))
	            {
					AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(this.radius()+0.30000001192092896D);
                    RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(this.getPositionVector(), this.hit.hitVec);

                    if (raytraceresult != null)
                    {
                    	raytraceresult = new RayTraceResult(entity);
                    	if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
                        {
                            this.onImpact(raytraceresult);
                            this.coolDown=this.attackCooldown();
                            if(!this.piercing())
                            	return;
                        }
                    }
	            }
			}
		}

    }

	protected abstract void onImpact(RayTraceResult result);

	public int livingTicks()
	{
		return this.livingTicks;
	}
	
	public int attackCooldown()
	{
		return 20;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.dataManager.set(shooterUUID, compound.getString("Shooter"));
		this.shooter=this.getShooter();
		this.livingTicks=compound.getInteger("LivingTicks");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
    	compound.setString("Shooter", this.dataManager.get(shooterUUID));
        compound.setInteger("LivingTicks", this.livingTicks);
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