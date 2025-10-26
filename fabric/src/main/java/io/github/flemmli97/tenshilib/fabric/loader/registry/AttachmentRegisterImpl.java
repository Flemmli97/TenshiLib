package io.github.flemmli97.tenshilib.fabric.loader.registry;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHolder;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.AttachmentRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class AttachmentRegisterImpl implements AttachmentRegister {

    public static final ResourceKey<? extends Registry<AttachmentType<?>>> KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "attachment_types"));
    public static final Registry<AttachmentType<?>> REGISTRY = LoaderRegistryAccess.INSTANCE.newRegistry(KEY, null, true, false).registry();

    @Override
    public AttachmentRegistry of(String modid) {
        return new AttachmenRegistryImpl(modid);
    }

    @Override
    public <T> T getAttachment(Entity entity, AttachmentType<T> type) {
        return ((AttachmentHolder) entity).tenshilib$getAttachment(type);
    }

    @Override
    public <T> Optional<T> getOptionalAttachment(Entity entity, AttachmentType<T> type) {
        return ((AttachmentHolder) entity).tenshilib$getExistingAttachment(type);
    }

    @Override
    public <T> T getAttachment(BlockEntity blockEntity, AttachmentType<T> type) {
        return ((AttachmentHolder) blockEntity).tenshilib$getAttachment(type);
    }

    @Override
    public <T> Optional<T> getOptionalAttachment(BlockEntity blockEntity, AttachmentType<T> type) {
        return ((AttachmentHolder) blockEntity).tenshilib$getExistingAttachment(type);
    }

    static class AttachmenRegistryImpl implements AttachmentRegistry {

        private final VanillaRegisterHandler<AttachmentType<?>> register;

        AttachmenRegistryImpl(String modid) {
            this.register = new VanillaRegisterHandler<>(KEY, modid);
        }

        @Override
        public <T> Supplier<AttachmentType<T>> register(String name, AttachmentType.Builder<T> builder) {
            return this.register.register(name, builder::build);
        }

        @Override
        public void registerContent() {
            this.register.registerContent();
        }
    }
}
