package io.github.flemmli97.tenshilib.platform;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface ClientCalls {

    ClientCalls INSTANCE = InitUtil.getPlatformInstance(ClientCalls.class,
            "io.github.flemmli97.tenshilib.fabric.platform.ClientCallsImpl",
            "io.github.flemmli97.tenshilib.forge.platform.ClientCallsImpl");

    RenderType getBeamRenderType(ResourceLocation loc);
}
