package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Expands vertex format array to accommodate potential expanded register
 */
@Mixin(VertexFormat.class)
public class VertexFormatMixin {

    @Mutable
    @Shadow
    @Final
    private int[] offsetsByElement;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;reduce(ILjava/util/function/IntBinaryOperator;)I", shift = At.Shift.AFTER, unsafe = true))
    private void onUpdateArray(List<VertexFormatElement> elements, List<String> names, IntList offsets, int vertexSize, CallbackInfo ci) {
        if (VertexFormatElement.MAX_COUNT != VertexFormatElementAccessor.fetchIdLookup().length) {
            this.offsetsByElement = new int[VertexFormatElementAccessor.fetchIdLookup().length];
        }
    }
}
