package io.github.flemmli97.tenshilib.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.tenshilib.fabric.client.SeparateTransformsModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getTransforms()Lnet/minecraft/client/renderer/block/model/ItemTransforms;",
            shift = At.Shift.BEFORE), argsOnly = true)
    private BakedModel modifyModel(BakedModel orig, @Local(argsOnly = true) ItemDisplayContext displayContext) {
        if (orig instanceof SeparateTransformsModel.Baked baked) {
            return baked.getContextModel(displayContext);
        }
        return orig;
    }
}
