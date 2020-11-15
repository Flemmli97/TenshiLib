package com.flemmli97.tenshilib.client.model;


import com.flemmli97.tenshilib.mixin.ModelRendererAccessors;
import net.minecraft.client.renderer.model.ModelRenderer;

public interface IResetModel {

    void resetModel();

    default void resetChild(ModelRenderer model) {
        for (ModelRenderer child : ((ModelRendererAccessors) model).getChildModels()) {
            if (child instanceof ModelRendererPlus)
                ((ModelRendererPlus) child).reset();
            this.resetChild(child);
        }
    }
}
