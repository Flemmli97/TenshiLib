package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.client.events.PlayerRotationEvent;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> {

    @SuppressWarnings("unchecked")
    @Inject(method = "setAngles", at = @At(value = "RETURN")) //mcp: setRotationAngles
    private void rotationHook(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo info){
        MinecraftForge.EVENT_BUS.post(new PlayerRotationEvent<T>(entity, (PlayerModel<T>) (Object) this, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch));
    }
}
