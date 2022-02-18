package io.github.flemmli97.tenshilib.fabric.platform;

import io.github.flemmli97.tenshilib.platform.ClientCalls;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ClientCallsImpl extends ClientCalls {

    public static void init() {
        INSTANCE = new ClientCallsImpl();
    }

    @Override
    public RenderType getBeamRenderType(ResourceLocation loc) {
        return RenderType.entityTranslucent(loc);
    }
}
