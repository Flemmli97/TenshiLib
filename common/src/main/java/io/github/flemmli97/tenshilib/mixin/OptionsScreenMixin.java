package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.patreon.client.PatreonClientUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsSubScreen.class)
public abstract class OptionsScreenMixin {

    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void onReposition(CallbackInfo ci) {
        PatreonClientUtil.addPatreonButton((Screen) (Object) this);
    }

}
