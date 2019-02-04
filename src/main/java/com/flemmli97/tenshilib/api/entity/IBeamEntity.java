package com.flemmli97.tenshilib.api.entity;

import net.minecraft.util.math.Vec3d;

public interface IBeamEntity {

	public Vec3d startVec();
	
	public Vec3d hitVec();
	
	public int livingTickMax();
}
