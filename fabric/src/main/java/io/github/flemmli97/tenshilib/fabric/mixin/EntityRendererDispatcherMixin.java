package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.fabric.platform.patreon.ClientPatreonImpl;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererDispatcherMixin {

    @Shadow
    private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    private void initRendererHook(ResourceManager resourceManager, CallbackInfo info) {
        ClientPatreonImpl.addPatreonLayer(this.playerRenderers);
    }
}
