package io.github.flemmli97.tenshilib.fabric.loader.registry;

import io.github.flemmli97.tenshilib.fabric.mixin.BuiltinRegistryAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeferredRegistrationHandler {

    private static boolean REGISTERED;
    static final Map<ResourceLocation, List<VanillaRegisterHandler<?>>> DEFERRED = new HashMap<>();
    private static final Comparator<ResourceLocation> NAMESPACE_FIRST = Comparator.comparing(ResourceLocation::getNamespace)
            .thenComparing(ResourceLocation::getPath);

    /**
     * Mimics order of neoforge impl
     */
    private static Set<ResourceLocation> getRegistrationOrder() {
        Set<ResourceLocation> ordered = new LinkedHashSet<>();
        ordered.add(Registries.ATTRIBUTE.location());
        ordered.add(Registries.DATA_COMPONENT_TYPE.location());
        ordered.add(Registries.ARMOR_MATERIAL.location());
        ordered.addAll(BuiltinRegistryAccessor.getLoaders().keySet());
        ordered.addAll(DEFERRED.keySet().stream().sorted(NAMESPACE_FIRST).toList());
        return ordered;
    }

    public static void finalizeRegister() {
        if (!REGISTERED) {
            Set<ResourceLocation> keys = getRegistrationOrder();
            for (ResourceLocation key : keys) {
                List<VanillaRegisterHandler<?>> deferred = DEFERRED.get(key);
                if (deferred != null)
                    deferred.forEach(VanillaRegisterHandler::finalizeRegister);
            }
            DEFERRED.clear();
            REGISTERED = true;
        }
    }

    static void add(ResourceLocation key, VanillaRegisterHandler<?> handler) {
        if (REGISTERED) {
            throw new RuntimeException("Tried to register content after registering phase! Culprit: " + handler);
        }
        DeferredRegistrationHandler.DEFERRED.computeIfAbsent(key, k -> new ArrayList<>()).add(handler);
    }
}
