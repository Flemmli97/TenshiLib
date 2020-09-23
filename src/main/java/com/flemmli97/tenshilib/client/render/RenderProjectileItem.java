package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public abstract class RenderProjectileItem<T extends Entity> extends EntityRenderer<T> {

    public RenderProjectileItem(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        /*stack.push();
        GlStateManager.translate(x, y + 0.2, z);
        GlStateManager.enableRescaleNormal();
        switch(this.getRenderType(entity)){
            case NORMAL:
                GlStateManager.rotate(-this.renderManager.playerViewY, 0, 1, 0);
                GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1, 0, 0);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEAPON:
                GlStateManager.rotate((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick) + 180, 0, 1, 0);
                GlStateManager.rotate((entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick) + 95, 1, 0, 0);
                GlStateManager.rotate(180, 1, 0, 0);
                break;
        }
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if(this.renderOutlines){
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        Minecraft.getInstance().getItemRenderer().renderItem(this.getRenderItemStack(entity), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        if(this.renderOutlines){
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        stack.pop();
        if(!this.renderOutlines){
            this.renderName(entity, x, y, z);
        }*/
    }

    /*@Override
    public ResourceLocation getEntityTexture(T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }*/

    public abstract ItemStack getRenderItemStack(T entity);

    /**
     * weapons get rotated so e.g. a swords tip points towards the travel direction
     */
    public abstract RenderType getRenderType(T entity);

    public enum RenderType {
        NORMAL, WEAPON
    }
}
