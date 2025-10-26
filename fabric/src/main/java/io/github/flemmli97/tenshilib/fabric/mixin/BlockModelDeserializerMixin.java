package io.github.flemmli97.tenshilib.fabric.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import io.github.flemmli97.tenshilib.fabric.client.BlockModelHandler;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(BlockModel.Deserializer.class)
public abstract class BlockModelDeserializerMixin {

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;", at = @At("HEAD"), cancellable = true)
    private void tryDeserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockModel> info) {
        BlockModel custom = BlockModelHandler.tryDeserialize(json, type, context);
        if (custom != null)
            info.setReturnValue(custom);
    }
}
