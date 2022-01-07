package io.github.flemmli97.tenshilib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface WorldRenderAccessor {

    @Invoker(value = "renderShape")
    static void drawShapeOutline(PoseStack stack, VertexConsumer buffer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        throw new IllegalStateException();
    }
}