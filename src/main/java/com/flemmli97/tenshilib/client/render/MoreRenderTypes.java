package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.mixin.RenderStateAccessors;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class MoreRenderTypes {

    private static final RenderState.DepthTestState NO_DEPTH = new RenderState.DepthTestState("never", 515);
    //public static final RenderType LINES_NO_DEPTH = RenderType.of("depthless_lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.builder().lineWidth(new RenderState.LineState(OptionalDouble.empty())).layering(RenderType.VIEW_OFFSET_Z_LAYERING).transparency(RenderType.TRANSLUCENT_TRANSPARENCY).target(RenderType.ITEM_TARGET).writeMaskState(RenderType.ALL_MASK).build(false));
    public static final RenderType TEXTURED_QUAD = RenderType.of("tenshilib:textured_quad", DefaultVertexFormats.POSITION_COLOR_TEXTURE, GL11.GL_QUADS, 256, RenderType.State.builder().cull(RenderStateAccessors.disableCull()).transparency(RenderStateAccessors.translucent()).alpha(RenderStateAccessors.oneTenthAlpha()).build(false));
    public static final RenderType LINE_NODEPTH = RenderType.of("tenshilib:line_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.State.builder().lineWidth(new RenderState.LineState(OptionalDouble.empty())).layering(RenderStateAccessors.offsetZLayer()).transparency(RenderStateAccessors.translucent()).depthTest(NO_DEPTH).target(RenderStateAccessors.itemTarget()).writeMaskState(RenderStateAccessors.allMask()).build(false));

}
