package io.github.flemmli97.tenshilib.patreon.client.model;

import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.resources.ResourceLocation;

public interface PatreonModelData<T> {

    void setRenderLocation(RenderLocation loc);

    ResourceLocation texture(T entity);
}
