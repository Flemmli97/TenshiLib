package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.mixin.RenderStateAccessors;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class MoreRenderTypes {

    private static final RenderState.DepthTestState NO_DEPTH = new RenderState.DepthTestState("never", GL11.GL_NEVER);

    public static final RenderType LINE_NODEPTH = RenderType.of("tenshilib:line_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, GL11.GL_ACCUM, RenderType.State.builder().lineWidth(new RenderState.LineState(OptionalDouble.empty())).layering(RenderStateAccessors.offsetZLayer()).transparency(RenderStateAccessors.translucent()).depthTest(NO_DEPTH).target(RenderStateAccessors.itemTarget()).writeMaskState(RenderStateAccessors.allMask()).build(false));

}
