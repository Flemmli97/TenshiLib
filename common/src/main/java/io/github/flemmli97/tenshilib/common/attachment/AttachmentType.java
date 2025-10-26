package io.github.flemmli97.tenshilib.common.attachment;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Crossplatform attachment type
 */
public class AttachmentType<T> {

    private final Function<Object, T> defaultValueSupplier;
    @Nullable
    private final AttachmentTransferHandler<T> transferHandler;

    private AttachmentType(Function<Object, T> defaultValueSupplier, @Nullable AttachmentTransferHandler<T> transferHandler) {
        this.defaultValueSupplier = defaultValueSupplier;
        this.transferHandler = transferHandler;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValueSupplier) {
        return builder(v -> defaultValueSupplier.get());
    }

    public static <T> Builder<T> builder(Function<Object, T> defaultValueSupplier) {
        return new Builder<>(defaultValueSupplier);
    }

    protected AttachmentType(AttachmentType<T> of) {
        this(of.defaultValueSupplier, of.transferHandler);
    }

    public Function<Object, T> defaultValueSupplier() {
        return this.defaultValueSupplier;
    }

    public Optional<AttachmentTransferHandler<T>> transferHandler() {
        return Optional.ofNullable(this.transferHandler);
    }

    public static class Builder<T> {

        private final Function<Object, T> defaultValueSupplier;
        @Nullable
        private AttachmentTransferHandler<T> transferHandler;

        private Builder(Function<Object, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        public Builder<T> transferHandler(@Nullable AttachmentTransferHandler<T> transferHandler) {
            this.transferHandler = transferHandler;
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this.defaultValueSupplier, this.transferHandler);
        }
    }
}
