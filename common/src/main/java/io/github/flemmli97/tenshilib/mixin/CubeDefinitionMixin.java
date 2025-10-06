package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.client.model.DeformationChange;
import io.github.flemmli97.tenshilib.mixinhelper.CubeDefinitionExtension;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CubeDefinition.class)
public abstract class CubeDefinitionMixin implements CubeDefinitionExtension {

    @Shadow
    @Final
    @Mutable
    private CubeDeformation grow;

    @Shadow
    public abstract ModelPart.Cube bake(int texWidth, int texHeight);

    @Override
    public ModelPart.Cube tenshilib$bakeWith(DeformationChange deform, int texWidth, int texHeight) {
        CubeDeformation old = this.grow;
        this.grow = this.grow.extend(deform.growX(), deform.growY(), deform.growZ());
        ModelPart.Cube baked = this.bake(texWidth, texHeight);
        this.grow = old;
        return baked;
    }
}
