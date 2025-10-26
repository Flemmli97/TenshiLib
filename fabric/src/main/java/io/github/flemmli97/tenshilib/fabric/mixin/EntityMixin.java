package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHandler;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements AttachmentHolder {

    @Shadow
    public abstract RegistryAccess registryAccess();

    @Unique
    private IdentityHashMap<AttachmentType<?>, Object> tenshilib$dataAttachments = new IdentityHashMap<>();

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        this.tenshilib$dataAttachments = AttachmentHandler.readAttachments(this, compound.getCompound(AttachmentHandler.TAG_KEY), this.registryAccess());
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void saveData(CompoundTag compound, CallbackInfoReturnable<CompoundTag> info) {
        CompoundTag tag = AttachmentHandler.saveAttachments(this.tenshilib$dataAttachments, this.registryAccess());
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
