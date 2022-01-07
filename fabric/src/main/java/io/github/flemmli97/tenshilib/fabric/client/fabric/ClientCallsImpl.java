package io.github.flemmli97.tenshilib.fabric.client.fabric;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ClientCallsImpl {

    public static RenderType getBeamRenderType(ResourceLocation loc) {
        return RenderType.entityTranslucent(loc);
    }
}
