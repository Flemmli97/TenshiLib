package com.flemmli97.tenshilib.client.model;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

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

    public ModelRendererPlus(Model model) {
        super(model);
    }

    public ModelRendererPlus(Model model, int texOffX, int texOffY) {
        super(model, texOffX, texOffY);
    }

    public ModelRendererPlus(int textWidth, int textHeight, int texOffX, int texOffY) {
        super(textWidth, textHeight, texOffX, texOffY);
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

    @Override
    public void rotate(MatrixStack matrixStack) {
        super.rotate(matrixStack);
        if(this.scaleX != 1 || this.scaleY != 1 || this.scaleZ != 1)
            matrixStack.scale(this.scaleX, this.scaleY, this.scaleZ);
    }
}
