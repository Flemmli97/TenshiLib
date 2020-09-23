package com.flemmli97.tenshilib.mixin;

import net.minecraft.client.renderer.RenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderState.class)
public interface RenderStateAccessors {

    @Accessor(value = "DISABLE_CULLING")
    static RenderState.CullState disableCull() {
        return null;
    }

    @Accessor(value = "TRANSLUCENT_TRANSPARENCY")
    static RenderState.TransparencyState translucent(){
        return null;
    }

    @Accessor(value = "ONE_TENTH_ALPHA")
    static RenderState.AlphaState oneTenthAlpha(){
        return null;
    }

    @Accessor(value = "ITEM_TARGET")
    static RenderState.TargetState itemTarget(){
        return null;
    }

    @Accessor(value = "VIEW_OFFSET_Z_LAYERING")
    static RenderState.LayerState offsetZLayer(){
        return null;
    }

    @Accessor(value = "ALL_MASK")
    static RenderState.WriteMaskState allMask(){
        return null;
    }
}
