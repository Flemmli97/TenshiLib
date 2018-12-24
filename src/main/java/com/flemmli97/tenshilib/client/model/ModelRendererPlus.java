package com.flemmli97.tenshilib.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * Saves a default value for rotation points and angles so its easier to reset
 */
public class ModelRendererPlus extends ModelRenderer{

	private float defaultRotPointX;
	private float defaultRotPointY;
	private float defaultRotPointZ;

	private float defaultRotAngleX;
	private float defaultRotAngleY;
	private float defaultRotAngleZ;

	
    public ModelRendererPlus(ModelBase model, String boxNameIn)
    {
    	super(model, boxNameIn);
    }

    public ModelRendererPlus(ModelBase model)
    {
        super(model);
    }

    public ModelRendererPlus(ModelBase model, int texOffX, int texOffY)
    {
        super(model, texOffX, texOffY);
    }
    
    public void reset()
    {
    	this.rotateAngleX=this.defaultRotAngleX;
    	this.rotateAngleY=this.defaultRotAngleY;
    	this.rotateAngleZ=this.defaultRotAngleZ;
    	this.rotationPointX=this.defaultRotPointX;
    	this.rotationPointY=this.defaultRotPointY;
    	this.rotationPointZ=this.defaultRotPointZ;
    }
    
    public void setDefaultRotPoint(float rotPointX, float rotPointY, float rotPointZ)
    {
    	this.defaultRotPointX = rotPointX;
    	this.defaultRotPointY = rotPointY;
    	this.defaultRotPointZ = rotPointZ;
    }

    public void setDefaultValues(float rotPointX, float rotPointY, float rotPointZ, float rotAngleX, float rotAngleY, float rotAngleZ)
    {
    	this.defaultRotPointX = rotPointX;
    	this.defaultRotPointY = rotPointY;
    	this.defaultRotPointZ = rotPointZ;
    	this.defaultRotAngleZ = rotAngleX;
    	this.defaultRotAngleY = rotAngleY;
    	this.defaultRotAngleZ = rotAngleZ;
    }
}
