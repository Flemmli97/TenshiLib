package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RenderProjectileItem<T extends Entity> extends EntityRenderer<T> {

    protected float scaleX = 1, scaleY = 1, scaleZ = 1;

    public RenderProjectileItem(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        stack.push();
        stack.scale(this.scaleX, this.scaleY, this.scaleZ);
        stack.translate(0,0.15,0);
        switch(this.getRenderType(entity)){
            case NORMAL:
                stack.multiply(this.renderManager.getRotation());
                stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
                break;
            case WEAPON:
                stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90+ MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw)));
                stack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(135-MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));
                break;
        }
        Minecraft.getInstance().getItemRenderer().renderItem(this.getRenderItemStack(entity), ItemCameraTransforms.TransformType.GROUND, packedLight, OverlayTexture.DEFAULT_UV, stack, buffer);
        stack.pop();
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return PlayerContainer.BLOCK_ATLAS_TEXTURE;
    }

    public abstract ItemStack getRenderItemStack(T entity);

    /**
     * weapons get rotated so e.g. a swords tip points towards the travel direction
     */
    public abstract Type getRenderType(T entity);

    public enum Type {
        NORMAL, WEAPON
    }
}
