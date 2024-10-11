package io.github.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.tenshilib.client.TenshilibShaders;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;

public class ParticleRenderTypes {

    /**
     * TODO: needs testing
     */
    public static final ParticleRenderType TRANSLUCENTADD = (tesselator, manager) -> {
        RenderSystem.setShader(TenshilibShaders::getBlurredParticleShader);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    };
}
