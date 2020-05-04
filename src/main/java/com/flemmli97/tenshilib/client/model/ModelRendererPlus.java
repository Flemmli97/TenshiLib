package com.flemmli97.tenshilib.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Saves a default value for rotation points and angles so its easier to reset
 */
public class ModelRendererPlus extends ModelRenderer {

    private float defaultRotPointX;
    private float defaultRotPointY;
    private float defaultRotPointZ;

    private float defaultRotAngleX;
    private float defaultRotAngleY;
    private float defaultRotAngleZ;

    private float defaultScaleX = 1;
    private float defaultScaleY = 1;
    private float defaultScaleZ = 1;
    public float scaleX = 1;
    public float scaleY = 1;
    public float scaleZ = 1;

    private boolean compiled;
    /** The GL display list rendered by the Tessellator for this model */
    private int displayList;

    public ModelRendererPlus(ModelBase model, String boxNameIn) {
        super(model, boxNameIn);
    }

    public ModelRendererPlus(ModelBase model) {
        super(model);
    }

    public ModelRendererPlus(ModelBase model, int texOffX, int texOffY) {
        super(model, texOffX, texOffY);
    }

    public void reset() {
        this.rotateAngleX = this.defaultRotAngleX;
        this.rotateAngleY = this.defaultRotAngleY;
        this.rotateAngleZ = this.defaultRotAngleZ;
        this.rotationPointX = this.defaultRotPointX;
        this.rotationPointY = this.defaultRotPointY;
        this.rotationPointZ = this.defaultRotPointZ;
        this.scaleX = this.defaultScaleX;
        this.scaleY = this.defaultScaleY;
        this.scaleZ = this.defaultScaleZ;
    }

    public void setDefaultRotPoint(float rotPointX, float rotPointY, float rotPointZ) {
        this.defaultRotPointX = rotPointX;
        this.defaultRotPointY = rotPointY;
        this.defaultRotPointZ = rotPointZ;
    }

    public void setDefaultRotAngle(float rotAngleX, float rotAngleY, float rotAngleZ) {
        this.defaultRotAngleX = rotAngleX;
        this.defaultRotAngleY = rotAngleY;
        this.defaultRotAngleZ = rotAngleZ;
    }

    public void setDefaultValues(float rotPointX, float rotPointY, float rotPointZ, float rotAngleX, float rotAngleY, float rotAngleZ) {
        this.defaultRotPointX = rotPointX;
        this.defaultRotPointY = rotPointY;
        this.defaultRotPointZ = rotPointZ;
        this.defaultRotAngleX = rotAngleX;
        this.defaultRotAngleY = rotAngleY;
        this.defaultRotAngleZ = rotAngleZ;
    }

    public void setDefaultScale(float scaleX, float scaleY, float scaleZ) {
        this.defaultScaleX = scaleX;
        this.defaultScaleY = scaleY;
        this.defaultScaleZ = scaleZ;
    }

    @SideOnly(Side.CLIENT)
    public void render(float scale) {
        if (!this.isHidden)
            if (this.showModel) {
                GlStateManager.pushMatrix();
                if (!this.compiled)
                    this.compileDisplayList(scale);
                GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F)
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);

                if (this.rotateAngleY != 0.0F)
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);

                if (this.rotateAngleX != 0.0F)
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

                if(this.scaleX != 1 || this.scaleY != 1 || this.scaleZ != 1)
                    GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);

                GlStateManager.callList(this.displayList);

                if (this.childModels != null) {
                    for (ModelRenderer model : this.childModels) {
                        model.render(scale);
                    }
                }

                GlStateManager.popMatrix();
                GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
            }
    }

    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float scale) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        for (ModelBox box : this.cubeList)
            box.render(bufferbuilder, scale);
        GlStateManager.glEndList();
        this.compiled = true;
    }
}
