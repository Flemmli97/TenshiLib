package io.github.flemmli97.tenshilib.fabric.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

    @Accessor("textureMap")
    Map<String, Either<Material, String>> tenshilib$getTextureMap();
}
