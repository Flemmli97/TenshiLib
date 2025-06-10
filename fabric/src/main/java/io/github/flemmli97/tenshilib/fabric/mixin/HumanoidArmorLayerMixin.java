package io.github.flemmli97.tenshilib.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.tenshilib.common.item.DynamicArmorTextureItem;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Shadow
    protected abstract boolean usesInnerModel(EquipmentSlot slot);

    @ModifyExpressionValue(method = "renderArmorPiece",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ArmorMaterial$Layer;texture(Z)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation armorResourceLocationEx(ResourceLocation original, @Local(argsOnly = true) LivingEntity entity,
                                                     @Local(argsOnly = true) EquipmentSlot slot,
                                                     @Local ItemStack stack, @Local ArmorItem armorItem,
                                                     @Local ArmorMaterial.Layer layer) {
        if (armorItem instanceof DynamicArmorTextureItem item) {
            return item.getArmorTexture(stack, entity, slot, layer, this.usesInnerModel(slot));
        }
        return original;
    }
}
