package io.github.flemmli97.tenshilib.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.tenshilib.fabric.client.events.CameraViewEvent;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private float tenshilib$zRot;

    @Shadow
    private float partialTickTime;

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
    private void onSetup(Camera camera, float yRot, float xRot, Operation<Void> original) {
        CameraViewEvent.Instance instance = new CameraViewEvent.Instance(camera, this.partialTickTime, yRot, xRot, 0);
        CameraViewEvent.EVENT.invoker().handle(instance);
        this.tenshilib$zRot = instance.getRoll();
        original.call(camera, instance.getYaw(), instance.getPitch());
    }

    @WrapOperation(method = "setRotation", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", remap = false))
    private Quaternionf cameraInject(Quaternionf instance, float angleY, float angleX, float angleZ, Operation<Quaternionf> original) {
        return original.call(instance, angleY, angleX, this.tenshilib$zRot != 0 ? this.tenshilib$zRot * Mth.DEG_TO_RAD : angleZ);
    }
}
