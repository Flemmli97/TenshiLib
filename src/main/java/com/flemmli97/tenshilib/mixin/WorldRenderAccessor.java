package com.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRenderAccessor {

    @Invoker(value = "drawShapeOutline") //yarn: drawShapeOutline, drawShape
    static void drawShapeOutline(MatrixStack stack, IVertexBuilder buffer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        throw new IllegalStateException();
    }
}