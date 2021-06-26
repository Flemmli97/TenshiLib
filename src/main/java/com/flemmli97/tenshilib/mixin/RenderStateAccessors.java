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

    @Accessor(value = "ITEM_ENTITY_TARGET")
    static RenderState.TargetState itemTarget() {
        throw new IllegalStateException();
    }

    @Accessor(value = "TRANSLUCENT_TARGET")
    static RenderState.TargetState translucentTarget() {
        throw new IllegalStateException();
    }

    @Accessor(value = "WEATHER_TARGET")
    static RenderState.TargetState weatherTarget() {
        throw new IllegalStateException();
    }

    @Accessor(value = "SHADE_ENABLED")
    static RenderState.ShadeModelState enableShade() {
        throw new IllegalStateException();
    }

    @Accessor(value = "VIEW_OFFSET_Z_LAYERING")
    static RenderState.LayerState offsetZLayer() {
        throw new IllegalStateException();
    }

    @Accessor(value = "COLOR_DEPTH_WRITE")
    static RenderState.WriteMaskState allMask() {
        throw new IllegalStateException();
    }

    @Accessor("DEPTH_ALWAYS")
    static RenderState.DepthTestState alwaysDepthTest() {
        throw new IllegalStateException();
    }
}