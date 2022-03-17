package io.github.flemmli97.tenshilib.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {

    @Accessor("cubes")
    @Final
    List<ModelPart.Cube> getCubes();

    @Accessor("children")
    @Final
    Map<String, ModelPart> getChildren();
}
