package io.github.flemmli97.tenshilib.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class TenshiLibShaders {

    private static ShaderInstance PARTICLE_BLUR;

    public static void registerShader() {
        ShaderRegister.INSTANCE.register(TenshiLib.MODID, register -> {
            try {
                register.register(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "particle_blur"), DefaultVertexFormat.PARTICLE,
                        shaderInstance -> PARTICLE_BLUR = shaderInstance);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static ShaderInstance getParticleBlur() {
        if (PARTICLE_BLUR == null) {
            throw new IllegalStateException("Particle blur shader has not been initialised");
        }
        return PARTICLE_BLUR;
    }
}
