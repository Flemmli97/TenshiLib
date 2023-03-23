package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class RenderProjectileItem<T extends Entity> extends EntityRenderer<T> {

    protected float scaleX = 1, scaleY = 1, scaleZ = 1;

    public RenderProjectileItem(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        stack.scale(this.scaleX, this.scaleY, this.scaleZ);
        stack.translate(0, 0.15, 0);
        switch (this.getRenderType(entity)) {
            case NORMAL -> {
                stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                stack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case WEAPON -> {
                stack.mulPose(Axis.YP.rotationDegrees(90 + Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
                stack.mulPose(Axis.ZP.rotationDegrees(135 - Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
            }
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(this.getRenderItemStack(entity), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, stack, buffer, entity.level, entity.getId());
        stack.popPose();
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return InventoryMenu.BLOCK_ATLAS;
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
