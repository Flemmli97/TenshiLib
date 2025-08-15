package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {

    @Invoker("beginElement")
    long doBeginElement(VertexFormatElement element);
}
