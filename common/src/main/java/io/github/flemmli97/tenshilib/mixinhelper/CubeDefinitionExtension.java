package io.github.flemmli97.tenshilib.mixinhelper;

import io.github.flemmli97.tenshilib.client.model.DeformationChange;
import net.minecraft.client.model.geom.ModelPart;

public interface CubeDefinitionExtension {

    ModelPart.Cube tenshilib$bakeWith(DeformationChange deform, int texWidth, int texHeight);

}
