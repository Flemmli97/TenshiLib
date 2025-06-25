package io.github.flemmli97.tenshilib.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.flemmli97.tenshilib.client.model.ItemHolderModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders an item as a layer.
 * More configurable than vanillas {@link net.minecraft.client.renderer.entity.layers.ItemInHandLayer}
 */
public class ItemLayer<T extends LivingEntity, M extends EntityModel<T> & ItemHolderModel> extends RenderLayer<T, M> {

    private final ItemInHandRenderer itemInHandRenderer;

    public ItemLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
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
            this.renderItem(entity, rightStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, stack, buffer, light);
            this.renderItem(entity, leftStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, stack, buffer, light);
            stack.popPose();
        }
    }

    protected ItemStack heldItemLeft(T entity, boolean rightHanded) {
        return rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
    }

    protected ItemStack heldItemRight(T entity, boolean rightHanded) {
        return rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    protected void renderItem(T entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm hand, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            this.getParentModel().transform(hand, poseStack);
            boolean flag = hand == HumanoidArm.LEFT;
            this.getParentModel().postTransform(flag, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            this.itemInHandRenderer.renderItem(entity, stack, displayContext, flag, poseStack, buffer, light);
            poseStack.popPose();
        }
    }
}
