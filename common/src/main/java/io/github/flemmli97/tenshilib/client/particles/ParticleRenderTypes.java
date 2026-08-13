package io.github.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.flemmli97.tenshilib.client.VertexUtils;
import io.github.flemmli97.tenshilib.client.shader.TenshiLibShaders;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;

public class ParticleRenderTypes {

    public static final VertexFormat PARTICLE_BLUR = VertexFormat.builder().add("Position", VertexFormatElement.POSITION)
            .add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR)
            .add("UV2", VertexFormatElement.UV2)
            .add("UVMinMax", VertexUtils.VEC4f.get()).build();

    @SuppressWarnings("deprecation")
    public static final ParticleRenderType TRANSLUCENT_ADD_BLURRED = (tesselator, manager) -> {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(TenshiLibShaders::getParticleBlur);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        return tesselator.begin(VertexFormat.Mode.QUADS, PARTICLE_BLUR);
    };
}
