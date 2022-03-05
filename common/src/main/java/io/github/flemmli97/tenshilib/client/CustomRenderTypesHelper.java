package io.github.flemmli97.tenshilib.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Helper methods to create custom render types without accesswideners/transformers on dependent mod too.
 */
public class CustomRenderTypesHelper {

    public static RenderType.CompositeState.CompositeStateBuilder createBuilder() {
        return RenderType.CompositeState.builder();
    }

    public static RenderType createType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderType.CompositeState state) {
        return RenderType.create(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, state);
    }
}
