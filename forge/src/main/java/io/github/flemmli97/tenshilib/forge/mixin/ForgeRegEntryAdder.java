package io.github.flemmli97.tenshilib.forge.mixin;

import io.github.flemmli97.tenshilib.platform.registry.CustomRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRegistryEntry.class)
public abstract class ForgeRegEntryAdder<V extends CustomRegistryEntry<V>> implements IForgeRegistryEntry<V> {
}
