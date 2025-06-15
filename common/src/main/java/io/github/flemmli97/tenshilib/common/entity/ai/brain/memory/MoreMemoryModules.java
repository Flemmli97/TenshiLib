package io.github.flemmli97.tenshilib.common.entity.ai.brain.memory;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.CircleData;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.loader.registry.RegistryEntrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

public class MoreMemoryModules {

    public static final LoaderRegister<MemoryModuleType<?>> MODULES = LoaderRegistryAccess.INSTANCE.of(Registries.MEMORY_MODULE_TYPE, TenshiLib.MODID);

    public static final RegistryEntrySupplier<MemoryModuleType<?>, MemoryModuleType<CircleData>> CIRCLE_DATA = MODULES.register("circle_data",
            () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryEntrySupplier<MemoryModuleType<?>, MemoryModuleType<AnimationPlayHolder<?>>> ANIMATION_TO_PLAY =
            MODULES.register("animation_to_play", () -> new MemoryModuleType<>(Optional.empty()));
}
