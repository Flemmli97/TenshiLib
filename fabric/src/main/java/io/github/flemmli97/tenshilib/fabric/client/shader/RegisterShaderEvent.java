package io.github.flemmli97.tenshilib.fabric.client.shader;

import io.github.flemmli97.tenshilib.client.shader.ShaderRegister;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.io.IOException;

public interface RegisterShaderEvent {

    /**
     * Fabrics event does not allow for custom shader instances
     */
    Event<RegisterShaderEvent> EVENT = EventFactory.createArrayBacked(RegisterShaderEvent.class, callbacks -> context -> {
        for (RegisterShaderEvent callback : callbacks) {
            callback.registerShaders(context);
        }
    });

    void registerShaders(ShaderRegister register) throws IOException;
}