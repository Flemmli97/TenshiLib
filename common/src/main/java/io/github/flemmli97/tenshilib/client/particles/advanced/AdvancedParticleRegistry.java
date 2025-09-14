package io.github.flemmli97.tenshilib.client.particles.advanced;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.CirclingHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.ColorHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.EntityFollowHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.MotionHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.MoveToHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.ParticleMetaHandler;
import io.github.flemmli97.tenshilib.client.particles.advanced.handler.ScaleHandler;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleContainer;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleData;
import io.github.flemmli97.tenshilib.common.particle.ParticleHandlerType;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import io.github.flemmli97.tenshilib.mixin.ParticleEngineAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedParticleRegistry {

    private static final Map<ParticleHandlerType<?>, Factory<?>> MAP = new HashMap<>();

    public static void init() {
        if (!TenshiLib.clientRequired())
            return;
        register(TenshilibParticleHandlerTypes.COLOR.get(), ColorHandler::new);
        register(TenshilibParticleHandlerTypes.ENTITY_FOLLOW.get(), EntityFollowHandler::new);
        register(TenshilibParticleHandlerTypes.MOTION.get(), MotionHandler::new);
        register(TenshilibParticleHandlerTypes.MOVE_TO.get(), MoveToHandler::new);
        register(TenshilibParticleHandlerTypes.PARTICLE_META.get(), ParticleMetaHandler::new);
        register(TenshilibParticleHandlerTypes.SCALE.get(), ScaleHandler::new);
        register(TenshilibParticleHandlerTypes.CIRLING.get(), CirclingHandler::new);
    }

    /**
     * Register a particle handler.
     * <p>
     * On neoforge:
     * This should be called during {@link FMLClientSetupEvent} (do not queue it!)
     * <p>
     * On fabric:
     * This should be called during {@link ClientSetupModInitializer#clientSetup} (not the fabric client entry!)
     */
    public static synchronized <T extends AdvancedParticleData> void register(ParticleHandlerType<T> type, Factory<T> factory) {
        if (MAP.put(type, factory) != null) {
            throw new IllegalStateException("Type already registered " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AdvancedParticleData> AdvancedParticleHandler create(T data, Particle particle) {
        Factory<T> factory = (Factory<T>) MAP.get(data.getType());
        if (factory == null) {
            throw new IllegalStateException("Type not registered " + data.getType());
        }
        return factory.create(data, particle);
    }

    public static void verify() {
        if (!TenshiLib.clientRequired())
            return;
        List<ParticleHandlerType<?>> missing = new ArrayList<>();
        TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES.registry().forEach(t -> {
            if (!MAP.containsKey(t)) {
                missing.add(t);
            }
        });
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Unregistered handlers for " + missing);
        }
    }

    public static void createParticle(AdvancedParticleContainer container, double x, double y, double z) {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        Particle inner = ((ParticleEngineAccessor) engine).makeParticleInvoker(container.options(), x, y, z, 0, 0, 0);
        inner.setPos(x, y, z);
        inner.setParticleSpeed(0, 0, 0);
        List<AdvancedParticleHandler> list = new ArrayList<>();
        for (AdvancedParticleData data : container.data()) {
            try {
                list.add(create(data, inner));
            } catch (IllegalStateException e) {
                TenshiLib.LOGGER.error(e);
            }
        }
        Particle particle = new AdvancedParticle(inner, list, Minecraft.getInstance().level, x, y, z);
        engine.add(particle);
    }

    public interface Factory<T extends AdvancedParticleData> {

        AdvancedParticleHandler create(T data, Particle particle);
    }
}
