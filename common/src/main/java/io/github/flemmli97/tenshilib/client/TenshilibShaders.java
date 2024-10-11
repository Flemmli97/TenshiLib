package io.github.flemmli97.tenshilib.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.function.Consumer;

public class TenshilibShaders {

    private static ShaderInstance BLUR_PARTICLE_SHADER_INSTANCE;

    public static void registerShader(ShaderRegister register) {
        try {
            register.register(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "particle_blur"), DefaultVertexFormat.PARTICLE,
                    shaderInstance -> TenshilibShaders.BLUR_PARTICLE_SHADER_INSTANCE = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ShaderInstance getBlurredParticleShader() {
        return BLUR_PARTICLE_SHADER_INSTANCE;
    }

    public interface ShaderRegister {

        void register(ResourceLocation id, VertexFormat vertexFormat, Consumer<ShaderInstance> onLoad) throws IOException;

    }
}
