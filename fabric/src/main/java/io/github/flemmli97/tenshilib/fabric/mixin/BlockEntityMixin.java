package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHandler;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Optional;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements AttachmentHolder {

    @Unique
    private IdentityHashMap<AttachmentType<?>, Object> tenshilib$dataAttachments = new IdentityHashMap<>();

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void loadData(CompoundTag compound, HolderLookup.Provider registries, CallbackInfo info) {
        this.tenshilib$dataAttachments = AttachmentHandler.readAttachments(this, compound.getCompound(AttachmentHandler.TAG_KEY), registries);
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    private void saveData(CompoundTag compound, HolderLookup.Provider registries, CallbackInfo info) {
        CompoundTag tag = AttachmentHandler.saveAttachments(this.tenshilib$dataAttachments, registries);
        if (tag != null) {
            compound.put(AttachmentHandler.TAG_KEY, tag);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T tenshilib$getAttachment(AttachmentType<T> type) {
        return (T) this.tenshilib$dataAttachments.computeIfAbsent(type, k -> type.defaultValueSupplier().apply(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> tenshilib$getExistingAttachment(AttachmentType<T> type) {
        return (Optional<T>) Optional.of(this.tenshilib$dataAttachments.get(type));
    }

    @Override
    public <T> void tenshilib$setAttachment(AttachmentType<T> type, T value) {
        this.tenshilib$dataAttachments.put(type, value);
    }
}
