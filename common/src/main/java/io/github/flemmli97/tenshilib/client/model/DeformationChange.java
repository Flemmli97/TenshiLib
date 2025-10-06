package io.github.flemmli97.tenshilib.client.model;

public record DeformationChange(float growX, float growY, float growZ) {

    public static DeformationChange NONE = new DeformationChange(0);

    public DeformationChange(float grow) {
        this(grow, grow, grow);
    }
}
