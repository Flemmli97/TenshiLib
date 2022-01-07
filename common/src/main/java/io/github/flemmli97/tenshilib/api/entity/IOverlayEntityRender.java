package io.github.flemmli97.tenshilib.api.entity;

public interface IOverlayEntityRender {

    int overlayU(int orig);

    /**
     * See {@link io.github.flemmli97.tenshilib.mixin.OverlayTextureMixin} for correct v
     */
    int overlayV(int orig);
}
