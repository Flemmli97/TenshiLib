package io.github.flemmli97.tenshilib.platform.registry;

import com.google.common.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CustomRegistryEntry<V extends CustomRegistryEntry<V>> {

    @SuppressWarnings("UnstableApiUsage")
    private final TypeToken<V> token = new TypeToken<>(this.getClass()) {
    };
    private ResourceLocation registryName = null;

    @SuppressWarnings("unchecked")
    public V setRegistryName(ResourceLocation res) {
        if (this.getRegistryName() != null) {
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + res + " Old: " + this.getRegistryName());
        }
        this.registryName = res;
        return (V) this;
    }

    public @Nullable ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public Class<V> getRegistryType() {
        return (Class<V>) this.token.getRawType();
    }
}
