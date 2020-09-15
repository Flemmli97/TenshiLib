package com.flemmli97.tenshilib.client.model;


import net.minecraft.client.renderer.model.ModelRenderer;

public interface IResetModel {

    public void resetModel();

    public default void resetChild(ModelRenderer model) {
        for(ModelRenderer child : model.childModels){
            if(child instanceof ModelRendererPlus)
                ((ModelRendererPlus) child).reset();
            this.resetChild(child);
        }
    }
}
