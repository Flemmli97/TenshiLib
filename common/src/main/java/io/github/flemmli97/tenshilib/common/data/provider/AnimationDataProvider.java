package io.github.flemmli97.tenshilib.common.data.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationDefinitionContainer;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AnimationDataProvider implements DataProvider {

    private final Map<ResourceLocation, AnimationDefinitionContainer> data = new LinkedHashMap<>();

    private final PackOutput output;

    public AnimationDataProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void add();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.add();
        return CompletableFuture.allOf(this.data.entrySet().stream().map(entry -> {
            ResourceLocation res = entry.getKey();
            Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(res.getNamespace() + "/" + AnimationDataManager.ID.getPath() + "/" + res.getPath() + ".json");
            JsonElement obj = AnimationDefinitionContainer.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow();
            return DataProvider.saveStable(cache, obj, path);
        }).toArray(CompletableFuture<?>[]::new));
    }

    @Override
    public String getName() {
        return "Animation Definitions";
    }

    public void addDefinition(Holder<EntityType<?>> type, AnimationDefinitionContainer.Builder builder) {
        this.addDefinition(type.unwrapKey().get().location(), builder);
    }

    public void addDefinition(RegistryEntrySupplier<EntityType<?>, ?> type, AnimationDefinitionContainer.Builder builder) {
        this.addDefinition(type.getID(), builder);
    }

    public void addDefinition(ResourceLocation res, AnimationDefinitionContainer.Builder builder) {
        if (this.data.put(res, builder.build()) != null) {
            throw new IllegalStateException("Animation definition already added for " + res);
        }
    }
}
