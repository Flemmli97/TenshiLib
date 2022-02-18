package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.fabric.platform.registry.RegistryHelperImpl;
import io.github.flemmli97.tenshilib.platform.registry.RegistryHelper;

public class EarlyPlatformInitImpl {

    public static RegistryHelper init() {
        return new RegistryHelperImpl();
    }
}
