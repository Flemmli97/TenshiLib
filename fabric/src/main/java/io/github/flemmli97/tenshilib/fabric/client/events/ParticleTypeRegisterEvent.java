package io.github.flemmli97.tenshilib.fabric.client.events;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.particle.ParticleRenderType;

import java.util.ArrayList;
import java.util.List;

public interface ParticleTypeRegisterEvent {

    /**
     * ParticleRenderType are hardcoded in vanilla under under {@link net.minecraft.client.particle.ParticleEngine#RENDER_ORDER}
     * This event allows registering custom ParticleRenderType
     */
    Event<ParticleTypeRegisterEvent> EVENT = EventFactory.createArrayBacked(ParticleTypeRegisterEvent.class,
            (listeners) -> handler -> {
                for (ParticleTypeRegisterEvent event : listeners) {
                    event.handle(handler);
                }
            }
    );

    void handle(RenderTypeRegister register);

    class RenderTypeRegister {

        private final List<ParticleRenderType> renderTypes = new ArrayList<>();

        public void addRenderType(ParticleRenderType type, ParticleRenderType... before) {
            if (this.renderTypes.contains(type)) {
                throw new IllegalStateException("ParticleRenderType " + type + " already added!");
            }
            int idx = -1;
            for (ParticleRenderType renderType : before) {
                int index = this.renderTypes.indexOf(renderType);
                if (idx == -1 || (index != -1 && index < idx)) {
                    idx = index;
                }
            }
            if (idx == -1) {
                this.renderTypes.add(type);
            } else {
                this.renderTypes.add(idx, type);
            }
        }

        public List<ParticleRenderType> renderTypes() {
            return ImmutableList.copyOf(this.renderTypes);
        }
    }
}
