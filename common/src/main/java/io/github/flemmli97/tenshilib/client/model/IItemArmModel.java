package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface IItemArmModel {

    default void childTransform(PoseStack stack) {
        stack.translate(0.0D, 0.75D, 0.0D);
        stack.scale(0.5F, 0.5F, 0.5F);
    }

    void transform(HumanoidArm hand, PoseStack stack);

    /**
     * pre transform values -> post transform values
     * y->z and z -> -y
     */
    default void postTransform(boolean leftSide, PoseStack stack) {
        stack.translate((leftSide ? -1 : 1) / 16.0F, 0.125D, -0.625D);
    }
}
