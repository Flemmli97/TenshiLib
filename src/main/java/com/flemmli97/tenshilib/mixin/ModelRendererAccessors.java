package com.flemmli97.tenshilib.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelRenderer.class)
public interface ModelRendererAccessors {

    @Accessor
    ObjectList<ModelRenderer> getChildModels();
}
