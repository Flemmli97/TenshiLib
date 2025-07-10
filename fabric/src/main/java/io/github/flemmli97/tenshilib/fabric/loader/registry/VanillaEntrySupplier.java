package io.github.flemmli97.tenshilib.fabric.loader.registry;

import com.mojang.datafixers.util.Either;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VanillaEntrySupplier<T, I extends T> implements RegistryEntrySupplier<T, I>, Holder<T> {

    private final ResourceKey<T> key;
    private Holder<T> holder;

    protected VanillaEntrySupplier(ResourceKey<T> res) {
        this.key = res;
    }

    public void bind(Registry<T> registry) {
        this.holder = registry.getHolder(this.key).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public I get() {
        Objects.requireNonNull(this.holder, () -> "Holder not present: " + this.key);
        return (I) this.holder.value();
    }

    @Override
    public ResourceLocation getID() {
        return this.key.location();
    }

    @Override
    public ResourceKey<T> getKey() {
        return this.key;
    }

    @Override
    public Holder<T> asHolder() {
        return this.holder != null ? this.holder : this;
    }

    /// ========== Holder Impls

    @Override
    public T value() {
        return this.get();
    }

    @Override
    public boolean isBound() {
        return this.holder != null && this.holder.isBound();
    }

    @Override
    public boolean is(ResourceLocation location) {
        return location.equals(this.key.location());
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return resourceKey.equals(this.key);
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(this.key);
    }

    @Override
    public boolean is(TagKey<T> tag) {
        return this.holder != null && this.holder.is(tag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean is(Holder<T> holder) {
        return this.holder != null && this.holder.is(holder);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return this.holder != null ? this.holder.tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.holder != null && this.holder.canSerializeIn(owner);
    }
}
