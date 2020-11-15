package com.flemmli97.tenshilib.mixin;

import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Mixin into OverlayTexture to fill the unused row of the overlay with a gradient of colors instead of one solid red color.
 * Vanilla uses row 3 (for the red hurt animation) and 10 (for the rest).
 * 0: red
 * 1: green
 * 2: blue
 * 3: red default (solid red. the hurt animation red)
 * 4: yellow
 * 5: pink
 * 6: cyan
 * 7: orange
 * 8: purple
 * 9: magenta
 * 10: white default
 * 11: light-blue
 * 12: lime
 * 13: brown
 * 14: black
 * 15: red (a.k.a. unused)
 */
@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin {

    @Unique
    private int initCounter = -1;

    @Unique
    private int initLoops = 0;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = -1308622593, ordinal = 0))
    private int red(int orig) {
        return this.runecraftory_getred(orig);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16777215, ordinal = 0))
    private int white(int orig) {
        this.initCounter++;
        if (this.initCounter == 16) {
            this.initCounter = 0;
            this.initLoops++;
        }
        return this.runecraftory_getcolor();
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

    /**
     * r and b are swapped
     */
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
                return 0x0080ff;
            case 8:
                return 0xff0080;
            case 9:
                return 0xff64c9;
            case 10:
            case 15:
                return 0xffffff;
            case 11:
                return 0xff8040;
            case 12:
                return 0x8fff8f;
            case 13:
                return 0x21374d;
            case 14:
                return 0x000000;
        }
        return 0x0000ff;
    }
}