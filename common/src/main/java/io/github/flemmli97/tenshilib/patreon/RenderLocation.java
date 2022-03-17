package io.github.flemmli97.tenshilib.patreon;

public enum RenderLocation {

    CIRCLING,
    CIRCLINGREVERSE,
    HAT,
    HATNOARMOR,
    LEFTSHOULDER,
    RIGHTSHOULDER,
    BACK;

    public static boolean isHead(RenderLocation loc) {
        return loc == HAT || loc == HATNOARMOR;
    }

    public static boolean isCircling(RenderLocation loc) {
        return loc == CIRCLING || loc == CIRCLINGREVERSE;
    }
}
