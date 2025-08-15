package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public interface ItemHolderModel {

    float ONE_PIXEL = 1 / 16f;

    default void childTransform(PoseStack stack) {
        stack.translate(0.0D, 0.75D, 0.0D);
        stack.scale(0.5F, 0.5F, 0.5F);
    }

    /**
     * Transform to the bones pivot point
     */
    void transform(HumanoidArm hand, PoseStack stack);
}
