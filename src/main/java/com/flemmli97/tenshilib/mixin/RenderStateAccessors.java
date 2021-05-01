package com.flemmli97.tenshilib.mixin;

import net.minecraft.client.renderer.RenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderState.class)
public interface RenderStateAccessors {

    @Accessor(value = "TRANSLUCENT_TRANSPARENCY")
    static RenderState.TransparencyState translucent() {
        throw new IllegalStateException();
    }

    @Accessor(value = "ITEM_TARGET")
    static RenderState.TargetState itemTarget() {
        throw new IllegalStateException();
    }

    @Accessor(value = "VIEW_OFFSET_Z_LAYERING")
    static RenderState.LayerState offsetZLayer() {
        throw new IllegalStateException();
    }

    @Accessor(value = "ALL_MASK")
    static RenderState.WriteMaskState allMask() {
        throw new IllegalStateException();
    }

    @Accessor("ALWAYS_DEPTH_TEST")
    static RenderState.DepthTestState alwaysDepthTest() {
        throw new IllegalStateException();
    }
}