package io.github.flemmli97.tenshilib.client.model;

public interface ExtendedModel {

    default float getPartialTick() {
        return 1;
    }

    ModelPartsContainer getModel();
}
