package com.flemmli97.tenshilib.mixin;

import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Mixin into OverlayTexture to fill the first eight row of the overlay with a gradient of colors instead of one solid red color.
 * Vanilla uses row 3 (for the red hurt animation) and 10 (for the rest).
 * 0: red gradient
 * 1: green gradient
 * 2: blue gradient
 * 3: default
 * 4: yellow gradient
 * 5: pink gradient
 * 6: cyan gradient
 * 7: black gradient
 */
@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {

    @Unique
    private int initCounter = -1;

    @Unique
    private int initLoops = 0;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = -1308622593, ordinal = 0))
    private int red(int orig) {
        return this.runecraftory_getred(orig);
    }

    private int runecraftory_getred(int orig) {
        this.initCounter++;
        if (this.initCounter == 16) {
            this.initCounter = 0;
            this.initLoops++;
        }
        int k = (int) ((1.0F - (float) this.initCounter / 15.0F * 0.75F) * 255);
        return this.initLoops == 3 ? orig : k << 24 | this.runecraftory_getcolor();
    }

    private int runecraftory_getcolor() {
        switch (this.initLoops) {
            case 1:
                return 0x00ff00;
            case 2:
                return 0xff0000;
            case 4:
                return 0x00ffff;
            case 5:
                return 0xff00ff;
            case 6:
                return 0xffff00;
            case 7:
                return 0xffffff;
        }
        return 0x0000ff;
    }
}