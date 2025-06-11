package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.mixinhelper.EntityRenderDispatcherAccess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements EntityRenderDispatcherAccess {

    @Shadow
    private Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Unique
    private Matrix4f tenshilib$preRenderMatrix;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void cacheStack(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        this.tenshilib$preRenderMatrix = new Matrix4f(poseStack.last().pose());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", shift = At.Shift.AFTER))
    private void cacheStackClear(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        this.tenshilib$preRenderMatrix = null;
    }

    @Override
    public Map<EntityType<?>, EntityRenderer<?>> tenshilib$getRenderers() {
        return this.renderers;
    }

    @Override
    public Matrix4f tenshilib$getPreRenderMatrix() {
        if (this.tenshilib$preRenderMatrix == null)
            return null;
        return new Matrix4f(this.tenshilib$preRenderMatrix);
    }
}
