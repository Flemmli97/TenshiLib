package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class SwordTestMixin implements IAOEWeapon, IDualWeapon {

    @Override
    public float getRange() {
        return 8;
    }

    @Override
    public float getFOV() {
        return 30;
    }
}
