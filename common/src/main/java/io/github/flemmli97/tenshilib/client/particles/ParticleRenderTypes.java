package io.github.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.tenshilib.mixin.AbstractTextureAccessor;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;

public class ParticleRenderTypes {

    @SuppressWarnings("deprecation")
    public static final ParticleRenderType TRANSLUCENT_ADD_BLURRED = new AdvancedParticleType() {

        private boolean prevBlue, prevMipmap;

        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager manager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            AbstractTexture atlas = manager.getTexture(TextureAtlas.LOCATION_PARTICLES);
            this.prevBlue = ((AbstractTextureAccessor) atlas).getBlur();
            this.prevMipmap = ((AbstractTextureAccessor) atlas).getMipmap();
            atlas.setFilter(true, false);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(TextureManager manager) {
            manager.getTexture(TextureAtlas.LOCATION_PARTICLES).setFilter(this.prevBlue, this.prevMipmap);
        }
    };
}
