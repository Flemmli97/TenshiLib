package com.flemmli97.tenshilib.common.events;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ModelPlayerRenderEvent extends Event{

	private final ModelPlayer model;
	private final float limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch;
	private final Entity entity;
	public ModelPlayerRenderEvent(ModelPlayer model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
			float scaleFactor) {
		this.model=model;
		this.limbSwing=limbSwing;
		this.limbSwingAmount=limbSwingAmount;
		this.ageInTicks=ageInTicks;
		this.netHeadYaw=netHeadYaw;
		this.headPitch=headPitch;
		this.entity=entity;
	}
	
	public ModelPlayer getModel()
	{
		return this.model;
	}
	
	public float getLimbSwing()
	{
		return this.limbSwing;
	}
	
	public float getLimbSwingAmount()
	{
		return this.limbSwingAmount;
	}
	
	public float getAgeInTicks()
	{
		return this.ageInTicks;
	}
	
	public float getNetHeadYaw()
	{
		return this.netHeadYaw;
	}
	
	public float getHeadPitch()
	{
		return this.headPitch;
	}
	
	public Entity getEntity()
	{
		return this.entity;
	}
}
