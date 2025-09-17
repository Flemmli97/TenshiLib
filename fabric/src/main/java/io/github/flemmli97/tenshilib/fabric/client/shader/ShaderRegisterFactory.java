package io.github.flemmli97.tenshilib.fabric.client.shader;

import io.github.flemmli97.tenshilib.client.shader.ShaderRegister;

import java.util.function.Consumer;

public class ShaderRegisterFactory implements ShaderRegister.Factory {

    @Override
    public void register(String modid, Consumer<ShaderRegister> consumer) {
        RegisterShaderEvent.EVENT.register(consumer::accept);
    }
}
