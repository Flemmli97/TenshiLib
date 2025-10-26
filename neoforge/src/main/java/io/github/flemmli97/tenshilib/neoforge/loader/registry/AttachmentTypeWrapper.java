package io.github.flemmli97.tenshilib.neoforge.loader.registry;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentTransferHandler;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.common.attachment.SerializableAttachment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows converstion from the cross-platform type to a {@link net.neoforged.neoforge.attachment.AttachmentType}
 */
@EventBusSubscriber(modid = TenshiLib.MODID)
public class AttachmentTypeWrapper<T> extends AttachmentType<T> {

    private static final Set<AttachmentTypeWrapper<?>> NEO_ATTACHMENTS = new HashSet<>();

    private net.neoforged.neoforge.attachment.AttachmentType<T> neoAttachment;

    public AttachmentTypeWrapper(AttachmentType<T> type) {
        super(type);
    }

    public net.neoforged.neoforge.attachment.AttachmentType<T> getNeoAttachment() {
        if (this.neoAttachment == null) {
            this.neoAttachment = net.neoforged.neoforge.attachment.AttachmentType.builder(h -> this.defaultValueSupplier().apply(h))
                    .serialize(new IAttachmentSerializer<>() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public T read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                            T value = AttachmentTypeWrapper.this.defaultValueSupplier().apply(holder);
                            if (value instanceof SerializableAttachment<?, ?> ser) {
                                ((SerializableAttachment<Tag, ?>) ser).read(tag, provider);
                            }
                            return value;
                        }

                        @Override
                        public @Nullable Tag write(T value, HolderLookup.Provider provider) {
                            if (value instanceof SerializableAttachment<?, ?> ser) {
                                return ser.write(provider);
                            }
                            return null;
                        }
                    })
                    .copyHandler((object, iAttachmentHolder, arg) -> null)
                    .build();
            NEO_ATTACHMENTS.add(this);
        }
        return this.neoAttachment;
    }

    @SuppressWarnings("unchecked")
    private static void copyAttachments(Entity from, Entity to, boolean wasDeath) {
        for (AttachmentTypeWrapper<?> type : NEO_ATTACHMENTS) {
            Object data = from.getExistingData(type.getNeoAttachment()).orElse(null);
            if (data == null)
                continue;
            AttachmentTransferHandler<?> handler = type.transferHandler().orElse(null);
            if (handler != null) {
                net.neoforged.neoforge.attachment.AttachmentType<Object> attachmentType = (net.neoforged.neoforge.attachment.AttachmentType<Object>) type.getNeoAttachment();
                Object newAttachment = ((AttachmentTransferHandler<Object>) handler)
                        .copy(data, to, wasDeath);
                if (newAttachment != null) {
                    to.setData(attachmentType, newAttachment);
                }
            }
        }
    }

    /**
     * Neos implementation does not pass in whether entity was from death or not so different handling for those cases is not possible
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        copyAttachments(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void onLivingConvert(LivingConversionEvent.Post event) {
        copyAttachments(event.getEntity(), event.getOutcome(), true);
    }
}
