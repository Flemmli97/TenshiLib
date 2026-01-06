package io.github.flemmli97.tenshilib.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.MeshData;
import io.github.flemmli97.tenshilib.client.particles.AdvancedParticleType;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ParticleEngine.class)
public abstract class NeoForgeParticleEngineMixin {

    @Shadow
    @Final
    private TextureManager textureManager;

    @ModifyVariable(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;build()Lcom/mojang/blaze3d/vertex/MeshData;"))
    private MeshData onEnd(MeshData value, @Local ParticleRenderType type) {
        if (value == null && type instanceof AdvancedParticleType adv)
            adv.end(this.textureManager);
        return value;
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V", shift = At.Shift.AFTER))
    private void onEnd(LightTexture texture, Camera camera, float partialTick, Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate, CallbackInfo info, @Local ParticleRenderType type) {
        if (type instanceof AdvancedParticleType adv)
            adv.end(this.textureManager);
    }
}
