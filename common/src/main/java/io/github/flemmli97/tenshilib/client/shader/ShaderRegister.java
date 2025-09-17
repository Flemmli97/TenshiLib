package io.github.flemmli97.tenshilib.client.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.flemmli97.tenshilib.loader.LoaderInitializer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.function.Consumer;

public interface ShaderRegister {

    Factory INSTANCE = LoaderInitializer.getImplInstance(Factory.class,
            "io.github.flemmli97.tenshilib.fabric.client.shader.ShaderRegisterFactory",
            "io.github.flemmli97.tenshilib.neoforge.client.shader.ShaderRegisterFactory");

    default ShaderInstance create(ResourceLocation location, VertexFormat format) throws IOException {
        return this.create(location, format, true);
    }

    ShaderInstance create(ResourceLocation location, VertexFormat format, boolean irisIgnore) throws IOException;

    default void register(ResourceLocation location, VertexFormat format, Consumer<ShaderInstance> loadCallback) throws IOException {
        this.register(this.create(location, format), loadCallback);
    }

    void register(ShaderInstance shaderInstance, Consumer<ShaderInstance> loadCallback);

    interface Factory {

        void register(String modid, Consumer<ShaderRegister> consumer);
    }
}