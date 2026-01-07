package io.github.flemmli97.tenshilib.common.attachment;

import io.github.flemmli97.tenshilib.loader.registry.AttachmentRegister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Crossplatform attachment type
 * The generic type paramenter H is simply used for sanity checks
 */
public class AttachmentType<H, T> {

    private final Function<Object, T> defaultValueSupplier;
    @Nullable
    private final AttachmentTransferHandler<T> transferHandler;

    private AttachmentType(Function<Object, T> defaultValueSupplier, @Nullable AttachmentTransferHandler<T> transferHandler) {
        this.defaultValueSupplier = defaultValueSupplier;
        this.transferHandler = transferHandler;
    }

    public static <H, T> Builder<H, T> builder(Supplier<T> defaultValueSupplier) {
        return builder(v -> defaultValueSupplier.get());
    }

    public static <H, T> Builder<H, T> builder(Function<H, T> defaultValueSupplier) {
        return new Builder<>(defaultValueSupplier);
    }

    protected AttachmentType(AttachmentType<H, T> of) {
        this(of.defaultValueSupplier, of.transferHandler);
    }

    public Function<Object, T> defaultValueSupplier() {
        return this.defaultValueSupplier;
    }

    public Optional<AttachmentTransferHandler<T>> transferHandler() {
        return Optional.ofNullable(this.transferHandler);
    }

    /**
     * Shortcut to platform implementations.
     * Gets the current attachment on the given holder. Creates a new one if it doesn't exist
     * <p>
     * Only {@link Entity} and {@link BlockEntity} supported!
     */
    public T get(H holder) {
        if (holder instanceof Entity entity) {
            return AttachmentRegister.INSTANCE.getAttachment(entity, this);
        }
        if (holder instanceof BlockEntity blockEntity) {
            return AttachmentRegister.INSTANCE.getAttachment(blockEntity, this);
        }
        throw new IllegalStateException("Unsupported attachment holder");
    }

    /**
     * Shortcut to platform implementations.
     * Gets the current attachment on the given holder if exists
     * <p>
     * Only {@link Entity} and {@link BlockEntity} supported!
     */
    public Optional<T> getOptional(H holder) {
        if (holder instanceof Entity entity) {
            return AttachmentRegister.INSTANCE.getOptionalAttachment(entity, this);
        }
        if (holder instanceof BlockEntity blockEntity) {
            return AttachmentRegister.INSTANCE.getOptionalAttachment(blockEntity, this);
        }
        throw new IllegalStateException("Unsupported attachment holder");
    }

    public static class Builder<H, T> {

        private final Function<Object, T> defaultValueSupplier;
        @Nullable
        private AttachmentTransferHandler<T> transferHandler;

        private Builder(Function<H, T> defaultValueSupplier) {
            this.defaultValueSupplier = h -> tryGet(defaultValueSupplier, h);
        }

        @SuppressWarnings("unchecked")
        private static <H, D> D tryGet(Function<H, D> getter, Object holder) {
            try {
                return getter.apply((H) holder);
            } catch (ClassCastException e) {
                throw new IllegalStateException("Attachment not supported for holder " + holder);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T> void tryRun(Consumer<T> getter, Object holder) {
            try {
                getter.accept((T) holder);
            } catch (ClassCastException e) {
                throw new IllegalStateException("Attachment not supported for holder " + holder);
            }
        }

        public Builder<H, T> transferHandler(@Nullable AttachmentTransferHandlerBuilder<H, T> transferHandler) {
            this.transferHandler = transferHandler != null ?
                    new AttachmentTransferHandler<>() {
                        @Override
                        public T copy(T from, Object targetHolder, boolean wasDead) {
                            return Builder.<H, T>tryGet(h -> transferHandler.copy(from, h, wasDead), targetHolder);
                        }

                        @Override
                        public void onCopy(Object targetHolder) {
                            tryRun(transferHandler::onCopy, targetHolder);
                        }
                    } : null;
            return this;
        }

        public AttachmentType<H, T> build() {
            return new AttachmentType<>(this.defaultValueSupplier, this.transferHandler);
        }
    }

    public interface AttachmentTransferHandlerBuilder<H, T> {

        /**
         * @param from         The original attachment
         * @param targetHolder The new attachment holder
         * @param wasDead      Whether this was due to a death or not
         * @return The new attachment
         */
        T copy(T from, H targetHolder, boolean wasDead);


        /**
         * Callback for after the transfer. Useful for e.g. sending a sync packet
         *
         * @param targetHolder The new attachment holder
         */
        default void onCopy(H targetHolder) {
        }
    }
}
