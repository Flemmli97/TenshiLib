package io.github.flemmli97.tenshilib.common.data.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class CodecBasedProvider<T> implements DataProvider {

    protected final PackOutput output;
    protected final PackOutput.Target target;
    protected final String name, modid, directory;
    protected final Codec<T> codec;
    protected final CompletableFuture<HolderLookup.Provider> provider;

    protected final Map<ResourceLocation, T> contents = new HashMap<>();

    public CodecBasedProvider(PackOutput output, PackOutput.Target target, String modid, String directory, Codec<T> codec, CompletableFuture<HolderLookup.Provider> provider) {
        this(output, target, directory, modid, directory, codec, provider);
    }

    public CodecBasedProvider(PackOutput output, PackOutput.Target target, String name, String modid, String directory, Codec<T> codec, CompletableFuture<HolderLookup.Provider> provider) {
        this.output = output;
        this.target = target;
        this.name = name;
        this.modid = modid;
        this.directory = directory;
        this.codec = codec;
        this.provider = provider;
    }

    protected abstract void add(HolderLookup.Provider provider);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.provider.thenApply(provider -> {
            this.add(provider);
            return provider;
        }).thenCompose(provider -> {
            DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
            return CompletableFuture.allOf(this.contents.entrySet().stream().map(entry ->
                    CompletableFuture.supplyAsync(() -> this.encode(ops, entry.getValue()))
                            .thenCompose(element -> DataProvider.saveStable(cache, element, this.getPath(entry.getKey())))
            ).toArray(CompletableFuture[]::new));
        });
    }

    protected Path getPath(ResourceLocation id) {
        return this.output.getOutputFolder(this.target)
                .resolve(id.getNamespace())
                .resolve(this.directory)
                .resolve(id.getPath() + ".json");
    }

    protected JsonElement encode(DynamicOps<JsonElement> ops, T value) {
        return this.codec.encodeStart(ops, value).getOrThrow();
    }

    @Override
    public String getName() {
        return String.format("Provider %s for %s", this.name, this.modid);
    }
}
