package io.github.flemmli97.tenshilib.mixinhelper;

import com.mojang.blaze3d.vertex.PoseStack;

public interface PoseStackExt {

    /**
     * Like {@link PoseStack#pushPose()} but pushes the specified pose
     */
    static void pushPose(PoseStack stack, PoseStack.Pose pose) {
        ((PoseStackExt) stack).tenshilib$PushPose(pose);
    }

    void tenshilib$PushPose(PoseStack.Pose pose);
}
