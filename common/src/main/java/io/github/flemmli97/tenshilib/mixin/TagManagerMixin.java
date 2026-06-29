package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.mixinhelper.TagLoaderRegistryContext;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.Executor;

@Mixin(TagManager.class)
public class TagManagerMixin {

    @Shadow
    @Final
    private RegistryAccess registryAccess;

    @SuppressWarnings("unchecked")
    @ModifyVariable(method = "createLoader", at = @At("RETURN"))
    private <T> TagLoader<Holder<T>> load(TagLoader<Holder<T>> tagloader, ResourceManager resourceManager, Executor backgroundExecutor, RegistryAccess.RegistryEntry<T> entry) {
        ((TagLoaderRegistryContext<T>) tagloader).tenshilib$setRegistry(entry.value().key(), this.registryAccess);
        return tagloader;
    }
}
