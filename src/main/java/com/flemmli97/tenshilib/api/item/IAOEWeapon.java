package com.flemmli97.tenshilib.api.item;

/**
 * Items with modified attack range and aoe.
 */
public interface IAOEWeapon {

    public float getRange();

    /**
     * @return FOV of the weapons aoe area in degree. E.g. getFOV()==10 would mean all entities at the crosshair position +-
     *         10 degrees left and right. Max 180
     */
    public float getFOV();
}
