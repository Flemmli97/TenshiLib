package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.client.model.IItemArmModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * MC HeldItemLayer only fit for simple biped type models
 */
@OnlyIn(Dist.CLIENT)
public class ItemLayer<T extends LivingEntity, M extends EntityModel<T> & IItemArmModel> extends LayerRenderer<T, M> {

    public ItemLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack stack, IRenderTypeBuffer buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getPrimaryHand() == HandSide.RIGHT;
        ItemStack leftStack = this.heldItemLeft(entity, flag);
        ItemStack rightStack = this.heldItemRight(entity, flag);
        if (!leftStack.isEmpty() || !rightStack.isEmpty()) {
            stack.push();
            if (this.getEntityModel().isChild)
                this.getEntityModel().childTransform(stack);
            this.renderItem(entity, rightStack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, stack, buffer, light);
            this.renderItem(entity, leftStack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, stack, buffer, light);
            stack.pop();
        }
    }

    protected ItemStack heldItemLeft(T entity, boolean rightHanded) {
        return rightHanded ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand();
    }

    protected ItemStack heldItemRight(T entity, boolean rightHanded) {
        return rightHanded ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
    }

    protected void renderItem(T entity, ItemStack stack, ItemCameraTransforms.TransformType transformType, HandSide hand, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (!stack.isEmpty()) {
            matrixStack.push();
            this.getEntityModel().transform(hand, matrixStack);
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
            boolean flag = hand == HandSide.LEFT;
            this.getEntityModel().postTransform(flag, matrixStack);
            Minecraft.getInstance().getFirstPersonRenderer().renderItem(entity, stack, transformType, flag, matrixStack, buffer, light);
            matrixStack.pop();
        }
    }
}
