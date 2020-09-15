package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class SwordTestMixin implements IDualWeapon {
}
