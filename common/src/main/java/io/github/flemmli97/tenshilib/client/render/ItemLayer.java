package io.github.flemmli97.tenshilib.client.render;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.client.model.IItemArmModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * MC HeldItemLayer only fit for simple biped type models
 */
public class ItemLayer<T extends LivingEntity, M extends EntityModel<T> & IItemArmModel> extends RenderLayer<T, M> {

    public ItemLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack leftStack = this.heldItemLeft(entity, flag);
        ItemStack rightStack = this.heldItemRight(entity, flag);
        if (!leftStack.isEmpty() || !rightStack.isEmpty()) {
            stack.pushPose();
            if (this.getParentModel().young)
                this.getParentModel().childTransform(stack);
            this.renderItem(entity, rightStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, stack, buffer, light);
            this.renderItem(entity, leftStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, stack, buffer, light);
            stack.popPose();
        }
    }

    protected ItemStack heldItemLeft(T entity, boolean rightHanded) {
        return rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
    }

    protected ItemStack heldItemRight(T entity, boolean rightHanded) {
        return rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    protected void renderItem(T entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm hand, PoseStack matrixStack, MultiBufferSource buffer, int light) {
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            this.getParentModel().transform(hand, matrixStack);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean flag = hand == HumanoidArm.LEFT;
            this.getParentModel().postTransform(flag, matrixStack);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, transformType, flag, matrixStack, buffer, light);
            matrixStack.popPose();
        }
    }
}
