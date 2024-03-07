package io.github.flemmli97.tenshilib.platform;

import io.github.flemmli97.tenshilib.TenshiLib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class InitUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getPlatformInstance(Class<T> abstractClss, String... impls) {
        if (impls == null || impls.length == 0)
            throw new IllegalStateException("Couldn't create an instance of " + abstractClss + ". No implementations provided!");
        Class<?> clss = null;
        int i = 0;
        while (clss == null && i < impls.length) {
            try {
                clss = Class.forName(impls[i]);
            } catch (ClassNotFoundException ignored) {
            }
            i++;
        }
        if (clss == null)
            TenshiLib.LOGGER.fatal("No Implementation of " + abstractClss + " found with given paths " + Arrays.toString(impls));
        else if (abstractClss.isAssignableFrom(clss)) {
            try {
                Constructor<T> constructor = (Constructor<T>) clss.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                TenshiLib.LOGGER.fatal("Implementation of " + clss + " needs to provide an no arg constructor");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Couldn't create an instance of " + abstractClss);
    }
}
