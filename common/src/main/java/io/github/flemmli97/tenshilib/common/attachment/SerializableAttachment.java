package io.github.flemmli97.tenshilib.common.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Implement this for an attachment to make it persistent
 */
public interface SerializableAttachment<S extends Tag, T> {

    T read(S tag, HolderLookup.Provider provider);

    @Nullable
    S write(HolderLookup.Provider provider);
}