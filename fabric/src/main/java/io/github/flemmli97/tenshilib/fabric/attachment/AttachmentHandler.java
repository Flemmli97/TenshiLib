package io.github.flemmli97.tenshilib.fabric.attachment;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentTransferHandler;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.common.attachment.SerializableAttachment;
import io.github.flemmli97.tenshilib.fabric.loader.registry.AttachmentRegisterImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;

public class AttachmentHandler {

    public static final String TAG_KEY = TenshiLib.MODID + ":Attachments";

    @SuppressWarnings("unchecked")
    public static void copyAttachments(Object from, Object to, boolean wasDeath) {
        if (!(from instanceof AttachmentHolder) || !(to instanceof AttachmentHolder)) {
            return;
        }
        AttachmentRegisterImpl.REGISTRY.forEach(type -> {
            AttachmentTransferHandler<Object> handler = (AttachmentTransferHandler<Object>) type.transferHandler().orElse(null);
            if (handler != null) {
                AttachmentType<Object> attachmentType = (AttachmentType<Object>) type;
                Object newAttachment = handler
                        .copy(((AttachmentHolder) from).tenshilib$getAttachment(type), to, wasDeath);
                if (newAttachment != null) {
                    ((AttachmentHolder) to).tenshilib$setAttachment(attachmentType, newAttachment);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static IdentityHashMap<AttachmentType<?>, Object> readAttachments(Object holder, CompoundTag tag, HolderLookup.Provider provider) {
        IdentityHashMap<AttachmentType<?>, Object> map = new IdentityHashMap<>();
        for (String key : tag.getAllKeys()) {
            AttachmentType<?> type = AttachmentRegisterImpl.REGISTRY.get(ResourceLocation.parse(key));
            if (type == null) {
                TenshiLib.LOGGER.warn("No such attachment type {} registered!", key);
                continue;
            }
            Object attachment = type.defaultValueSupplier().apply(holder);
            if (attachment instanceof SerializableAttachment<?, ?> ser) {
                try {
                    ((SerializableAttachment<Tag, ?>) ser).read(tag.get(key), provider);
                    map.put(type, attachment);
                } catch (Exception exception) {
                    TenshiLib.LOGGER.warn("Could not read attachment {}!", key, exception);
                }
            }
        }
        return map;
    }

    public static CompoundTag saveAttachments(IdentityHashMap<AttachmentType<?>, ?> attachments, HolderLookup.Provider provider) {
        if (attachments.isEmpty())
            return null;
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<AttachmentType<?>, ?> entry : attachments.entrySet()) {
            ResourceLocation key = AttachmentRegisterImpl.REGISTRY.getKey(entry.getKey());
            if (key == null) {
                TenshiLib.LOGGER.warn("Attachment type {} is not registered!", entry.getKey());
                continue;
            }
            if (entry.getValue() instanceof SerializableAttachment<?, ?> ser) {
                Tag tagEntry = ser.write(provider);
                if (tagEntry != null) {
                    tag.put(key.toString(), tagEntry);
                }
            }
        }
        if (tag.isEmpty())
            return null;
        return tag;
    }
}
