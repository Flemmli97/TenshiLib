package io.github.flemmli97.tenshilib.fabric.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

    @Accessor("parentLocation")
    ResourceLocation tenshilib$getParentLocation();

    @Accessor("elements")
    List<BlockElement> tenshilib$getElements();

    @Accessor("textureMap")
    Map<String, Either<Material, String>> tenshilib$getTextureMap();
}
