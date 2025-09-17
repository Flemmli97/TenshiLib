package io.github.flemmli97.tenshilib.fabric.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.MeshData;
import io.github.flemmli97.tenshilib.client.particles.AdvancedParticleType;
import io.github.flemmli97.tenshilib.client.particles.ParticleRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Searching for the cause took ages...
 * mojank hardcoding stuff again
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Mutable
    @Final
    @Shadow
    private static List<ParticleRenderType> RENDER_ORDER;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void customTypes(ClientLevel level, TextureManager textureManager, CallbackInfo ci) {
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER)
                .add(ParticleRenderTypes.TRANSLUCENT_ADD_BLURRED)
                .build();
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;build()Lcom/mojang/blaze3d/vertex/MeshData;"))
    private MeshData onEnd(MeshData value, @Local ParticleRenderType type) {
        if (value == null && type instanceof AdvancedParticleType adv)
            adv.end(this.textureManager);
        return value;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V", shift = At.Shift.AFTER))
    private void onEnd(LightTexture lightTexture, Camera camera, float partialTick, CallbackInfo info, @Local ParticleRenderType type) {
        if (type instanceof AdvancedParticleType adv)
            adv.end(this.textureManager);
    }
}
