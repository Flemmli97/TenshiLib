package com.flemmli97.tenshilib.common.world;

public enum LocationType {

	GROUND(-1, 0),
	UNDERGROUND(20, 20),
	AIR(100, 50);
	
	private int minHeight, randomization;
	LocationType(int minHeight, int randomization)
	{
		this.minHeight=minHeight;
		this.randomization=randomization;
	}
	
	public int minHeight()
	{
		return this.minHeight;
	}
	
	public int randomization()
	{
		return this.randomization;
	}
}
