package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.mixinhelper.PoseStackExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin implements PoseStackExt {

    @Shadow
    private Deque<PoseStack.Pose> poseStack;

    @Override
    public void pushPose(PoseStack.Pose pose) {
        this.poseStack.addLast(pose);
    }
}
