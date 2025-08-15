package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VertexFormatElement.class)
public interface VertexFormatElementMixin {

    @Accessor("BY_ID")
    static VertexFormatElement[] fetchIdLookup() {
        throw new IllegalStateException();
    }

    @Accessor("BY_ID")
    @Mutable
    static void updateIdLookup(VertexFormatElement[] array) {
        throw new IllegalStateException();
    }
}
