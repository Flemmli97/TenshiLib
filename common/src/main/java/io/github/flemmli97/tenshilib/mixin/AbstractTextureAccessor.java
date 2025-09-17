package io.github.flemmli97.tenshilib.mixin;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTexture.class)
public interface AbstractTextureAccessor {

    @Accessor("blur")
    boolean getBlur();

    @Accessor("mipmap")
    boolean getMipmap();

}
