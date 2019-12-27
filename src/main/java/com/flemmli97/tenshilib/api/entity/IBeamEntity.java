package com.flemmli97.tenshilib.api.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public interface IBeamEntity {

	public Vec3d startVec();
	
	public Vec3d hitVec();
	
	public int livingTickMax();
	
	public void updateYawPitch();
	
	@Nullable 
	public EntityLivingBase getShooter();
}
