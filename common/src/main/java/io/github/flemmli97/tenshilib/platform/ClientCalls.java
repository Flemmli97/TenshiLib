package io.github.flemmli97.tenshilib.platform;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public abstract class ClientCalls {

    protected static ClientCalls INSTANCE;

    public static ClientCalls instance() {
        return INSTANCE;
    }

    public abstract RenderType getBeamRenderType(ResourceLocation loc);
}
