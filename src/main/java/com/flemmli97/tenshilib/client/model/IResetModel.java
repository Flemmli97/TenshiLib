package com.flemmli97.tenshilib.client.model;

import net.minecraft.client.model.ModelRenderer;

public interface IResetModel {
	
	public void resetModel();

	public default void resetChild(ModelRenderer model) {
	    if(model.childModels!=null)
            for(ModelRenderer child : model.childModels)
            {
                if(child instanceof ModelRendererPlus)
                    ((ModelRendererPlus)child).reset();
                this.resetChild(child);
            }
	}
}
