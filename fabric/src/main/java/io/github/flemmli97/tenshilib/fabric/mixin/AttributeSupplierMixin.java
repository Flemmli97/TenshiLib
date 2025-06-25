package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.fabric.loader.events.EntityAttributeModifierEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.OptionalDouble;

@Mixin(AttributeSupplier.class)
public class AttributeSupplierMixin implements EntityAttributeModifierEvent.AttributeSupplierMerger {

    @Shadow
    @Final
    private Map<Holder<Attribute>, AttributeInstance> instances;

    @Override
    public AttributeSupplier mergeWith(Map<Holder<Attribute>, OptionalDouble> other) {
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        this.instances.forEach((att, inst) -> builder.add(att, inst.getBaseValue()));
        other.forEach((att, val) -> val.ifPresentOrElse(d -> builder.add(att, d), () -> builder.add(att)));
        return builder.build();
    }
}
