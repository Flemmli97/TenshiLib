package io.github.flemmli97.tenshilib;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.flemmli97.tenshilib.platform.registry.RegistryHelper;

public class EarlyPlatformInit {

    /**
     * Still need to use @ExpectPlatform cause we need the implementation earlier than its possible to init it normally.
     */
    @ExpectPlatform
    public static RegistryHelper init() {
        throw new AssertionError();
    }
}
