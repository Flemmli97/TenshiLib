package io.github.flemmli97.tenshilib.fabric.attachment;

import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;

import java.util.Optional;

/**
 * Fabrics implementation does neither have separate handling for copy data nor does it accept a function for type construction
 */
public interface AttachmentHolder {

    <T> T tenshilib$getAttachment(AttachmentType<?, T> type);

    <T> Optional<T> tenshilib$getExistingAttachment(AttachmentType<?, T> type);

    <T> void tenshilib$setAttachment(AttachmentType<?, T> type, T value);
}
