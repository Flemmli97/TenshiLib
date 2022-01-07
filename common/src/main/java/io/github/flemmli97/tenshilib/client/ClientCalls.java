package io.github.flemmli97.tenshilib.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ClientCalls {

    @ExpectPlatform
    public static RenderType getBeamRenderType(ResourceLocation loc) {
        throw new AssertionError();
    }
}
