package io.github.flemmli97.tenshilib.forge.mixin;

import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "io/github/flemmli97/tenshilib/platform/registry/RegEntryInternal")
public abstract class ForgeRegEntryAdder<V> implements IForgeRegistryEntry<V> {

}
