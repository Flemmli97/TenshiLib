package io.github.flemmli97.tenshilib.common.data.provider;

import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationsBuilder;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public abstract class AnimationDataProvider extends CodecBasedProvider<AnimationsBuilder> {

    public AnimationDataProvider(PackOutput output, String modid, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, PackOutput.Target.DATA_PACK, "Animation Data", modid, AnimationDataManager.DIRECTORY, AnimationsBuilder.CODEC, provider);
    }

    public void addDefinition(Holder<EntityType<?>> type, AnimationsBuilder animationsBuilder) {
        this.addDefinition(type.unwrapKey().orElseThrow().location(), animationsBuilder);
    }

    public void addDefinition(RegistryEntrySupplier<EntityType<?>, ?> type, AnimationsBuilder animationsBuilder) {
        this.addDefinition(type.getID(), animationsBuilder);
    }

    public void addDefinition(ResourceLocation res, AnimationsBuilder animationsBuilder) {
        if (this.contents.put(res, animationsBuilder) != null) {
            throw new IllegalStateException("Animation definition already added for " + res);
        }
    }
}
