package io.github.flemmli97.tenshilib.loader.registry;

import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.loader.LoaderInitializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Crossplatform registration for attachments.
 * Since neo and fabric have different implementations we can't use a normal {@link LoaderRegister}
 * Only attachments for entity and block entities are implemented as for level use {@link net.minecraft.world.level.saveddata.SavedData}
 */
public interface AttachmentRegister {

    AttachmentRegister INSTANCE = LoaderInitializer.getImplInstance(AttachmentRegister.class,
            "io.github.flemmli97.tenshilib.fabric.loader.registry.AttachmentRegisterImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.registry.AttachmentRegisterImpl");

    /**
     * Returns a new registry to register new attachments to
     */
    AttachmentRegistry of(String modid);

    default <T> T getAttachment(Entity entity, Supplier<AttachmentType<?, T>> type) {
        return this.getAttachment(entity, type.get());
    }

    /**
     * Get the attachment on the entity.
     * If it doesn't exist a new one will be created
     */
    <T> T getAttachment(Entity entity, AttachmentType<?, T> type);

    <T> Optional<T> getOptionalAttachment(Entity entity, AttachmentType<?, T> type);

    default <T> T getAttachment(BlockEntity blockEntity, Supplier<AttachmentType<?, T>> type) {
        return this.getAttachment(blockEntity, type.get());
    }

    /**
     * Get the attachment on the block entity.
     * If it doesn't exist a new one will be created
     */
    <T> T getAttachment(BlockEntity blockEntity, AttachmentType<?, T> type);

    <T> Optional<T> getOptionalAttachment(BlockEntity blockEntity, AttachmentType<?, T> type);

    interface AttachmentRegistry {

        <H, T> Supplier<AttachmentType<H, T>> register(String name, AttachmentType.Builder<H, T> builder);

        void registerContent();
    }
}
