package io.github.flemmli97.tenshilib.common.attachment;

/**
 * This is used to transfer attachment from one holder to another.
 * By default, this is used
 * - When the player gets cloned (which happens if it dies or moves from the end)
 * - When an entity is converted
 */
public interface AttachmentTransferHandler<T> {

    /**
     * @param from         The original attachment
     * @param targetHolder The new attachment holder
     * @param wasDead      Whether this was due to a death or not
     * @return The new attachment
     */
    T copy(T from, Object targetHolder, boolean wasDead);
}
