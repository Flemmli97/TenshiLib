package io.github.flemmli97.tenshilib.forge.platform;

import io.github.flemmli97.tenshilib.platform.ClientCalls;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;

public class ClientCallsImpl implements ClientCalls {

    @Override
    public RenderType getBeamRenderType(ResourceLocation loc) {
        return ForgeRenderTypes.getUnlitTranslucent(loc);
    }
}
