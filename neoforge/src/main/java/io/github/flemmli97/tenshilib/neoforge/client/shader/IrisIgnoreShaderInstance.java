package io.github.flemmli97.tenshilib.neoforge.client.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

/**
 * Shaders that should not be skipped with iris shaders enabled
 */
public class IrisIgnoreShaderInstance extends ShaderInstance {

    public IrisIgnoreShaderInstance(ResourceProvider provider, ResourceLocation location, VertexFormat format) throws IOException {
        super(provider, location, format);
    }

    public boolean iris$shouldSkipThis() {
        return false;
    }
}
