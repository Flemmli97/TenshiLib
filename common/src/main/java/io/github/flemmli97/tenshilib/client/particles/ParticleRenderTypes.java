package io.github.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;

public class ParticleRenderTypes {

    /**
     * From botanias particle types (vazkii.botania.client.fx.FXWisp)
     * This RenderType applies a blur to the particle sheet.
     * Neighboring particles thus overflow into the current particle!
     * Reducing the UV can circumvent this
     */
    public static final ParticleRenderType TRANSLUCENTADD = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager manager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            manager.getTexture(TextureAtlas.LOCATION_PARTICLES).setFilter(true, false);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
            Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES).setFilter(false, false);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    };
}
