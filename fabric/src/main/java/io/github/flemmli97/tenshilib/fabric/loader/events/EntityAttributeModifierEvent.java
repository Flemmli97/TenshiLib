package io.github.flemmli97.tenshilib.fabric.loader.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public interface EntityAttributeModifierEvent {

    /**
     * Allows modifying the default entity attributes in {@link net.minecraft.world.entity.ai.attributes.DefaultAttributes} safely.
     * Fabric API only allows adding to new entities
     */
    Event<Modify> EVENT = EventFactory.createArrayBacked(Modify.class,
            (listeners) -> event -> {
                for (Modify listener : listeners) {
                    listener.call(event);
                }
            }
    );

    interface Modify {

        void call(AttributeModifications modifications);
    }

    class AttributeModifications {

        private final List<EntityType<? extends LivingEntity>> types;
        private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> defaults;
        private final Map<EntityType<? extends LivingEntity>, Map<Holder<Attribute>, OptionalDouble>> modifications = new HashMap<>();

        public AttributeModifications(Map<EntityType<? extends LivingEntity>, AttributeSupplier> defaults) {
            this.types = ImmutableList.copyOf(defaults.keySet());
            this.defaults = defaults;
            defaults.keySet().forEach(type -> this.modifications.put(type, new LinkedHashMap<>()));
        }

        public AttributeModifications add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
            this.modify(type, attribute, OptionalDouble.empty());
            return this;
        }

        public AttributeModifications add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute, double baseValue) {
            this.modify(type, attribute, OptionalDouble.of(baseValue));
            return this;
        }

        private void modify(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute, OptionalDouble baseValue) {
            this.modifications.computeIfAbsent(type, k -> new LinkedHashMap<>())
                    .put(attribute, baseValue);
        }

        public boolean has(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
            AttributeSupplier sup = this.defaults.get(type);
            return sup.hasAttribute(attribute) || (this.modifications.get(type) != null && this.modifications.get(type).containsKey(attribute));
        }

        public List<EntityType<? extends LivingEntity>> getTypes() {
            return this.types;
        }

        public Map<EntityType<? extends LivingEntity>, Map<Holder<Attribute>, OptionalDouble>> view() {
            return ImmutableMap.copyOf(this.modifications);
        }
    }

    interface AttributeSupplierMerger {

        AttributeSupplier mergeWith(Map<Holder<Attribute>, OptionalDouble> other);
    }
}
