package io.github.flemmli97.tenshilib.neoforge.loader.registry;

import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.loader.registry.AttachmentRegister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class AttachmentRegisterImpl implements AttachmentRegister {

    @Override
    public AttachmentRegistry of(String modid) {
        return new AttachmenRegistryImpl(modid);
    }

    @Override
    public <T> T getAttachment(Entity entity, AttachmentType<T> type) {
        if (!(type instanceof AttachmentTypeWrapper<T> wrapper))
            throw new IllegalStateException("Unsupported type");
        return entity.getData(wrapper.getNeoAttachment());
    }

    @Override
    public <T> Optional<T> getOptionalAttachment(Entity entity, AttachmentType<T> type) {
        if (!(type instanceof AttachmentTypeWrapper<T> wrapper))
            throw new IllegalStateException("Unsupported type");
        return entity.getExistingData(wrapper.getNeoAttachment());
    }

    @Override
    public <T> T getAttachment(BlockEntity blockEntity, AttachmentType<T> type) {
        if (!(type instanceof AttachmentTypeWrapper<T> wrapper))
            throw new IllegalStateException("Unsupported type");
        return blockEntity.getData(wrapper.getNeoAttachment());
    }

    @Override
    public <T> Optional<T> getOptionalAttachment(BlockEntity blockEntity, AttachmentType<T> type) {
        if (!(type instanceof AttachmentTypeWrapper<T> wrapper))
            throw new IllegalStateException("Unsupported type");
        return blockEntity.getExistingData(wrapper.getNeoAttachment());
    }

    static class AttachmenRegistryImpl implements AttachmentRegistry {

        private final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> register;

        AttachmenRegistryImpl(String modid) {
            this.register = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, modid);
        }

        @Override
        public <T> Supplier<AttachmentType<T>> register(String name, AttachmentType.Builder<T> builder) {
            AttachmentTypeWrapper<T> wrapper = new AttachmentTypeWrapper<>(builder.build());
            DeferredHolder<?, ?> value = this.register.register(name, wrapper::getNeoAttachment);
            return () -> {
                // This makes it throw if it's not registered
                value.get();
                return wrapper;
            };
        }

        @Override
        public void registerContent() {
            IEventBus bus = ModList.get().getModContainerById(this.register.getNamespace())
                    .map(ModContainer::getEventBus).orElse(null);
            if (bus == null) {
                throw new IllegalStateException("Unable to get mod eventbus for modid " + this.register.getNamespace());
            }
            this.register.register(bus);
        }
    }
}
