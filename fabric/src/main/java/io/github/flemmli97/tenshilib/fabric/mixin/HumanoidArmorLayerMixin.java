package io.github.flemmli97.tenshilib.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.api.item.DynamicArmorTextureItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Shadow
    private static Map<String, ResourceLocation> ARMOR_LOCATION_CACHE;

    @Unique
    private LivingEntity tenshilibCacheLivingEntity;
    @Unique
    private ItemStack tenshilibCacheStack = ItemStack.EMPTY;
    @Unique
    private EquipmentSlot tenshilibEquipmentSlot;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void cacheVars(PoseStack poseStack, MultiBufferSource multiBufferSource, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel<?> humanoidModel, CallbackInfo info) {
        this.tenshilibCacheLivingEntity = livingEntity;
        this.tenshilibEquipmentSlot = equipmentSlot;
        this.tenshilibCacheStack = livingEntity.getItemBySlot(equipmentSlot);
    }

    @Inject(method = "getArmorLocation", at = @At("HEAD"), cancellable = true)
    private void armorResourceLocationEx(ArmorItem armorItem, boolean bl, @Nullable String string, CallbackInfoReturnable<ResourceLocation> info) {
        if (!this.tenshilibCacheStack.isEmpty() && armorItem instanceof DynamicArmorTextureItem item) {
            info.setReturnValue(ARMOR_LOCATION_CACHE.computeIfAbsent(item.getArmorTexture(this.tenshilibCacheStack, this.tenshilibCacheLivingEntity, this.tenshilibEquipmentSlot, null), ResourceLocation::new));
        }
    }
}
