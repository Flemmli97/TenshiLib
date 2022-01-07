package io.github.flemmli97.tenshilib.forge.client.forge;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;

public class ClientCallsImpl {

    public static RenderType getBeamRenderType(ResourceLocation loc) {
        return ForgeRenderTypes.getUnlitTranslucent(loc);
    }
}
