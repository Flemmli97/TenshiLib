package io.github.flemmli97.tenshilib.common.utils;

import io.github.flemmli97.tenshilib.TenshiLib;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class ResourceStream {

    public static InputStream getAssetsStream(String modid, String dir, String fileName) throws FileNotFoundException {
        InputStream input = ResourceStream.class.getResourceAsStream("/assets/" + modid + "/" + dir + "/" + fileName);
        if (input != null)
            return input;
        TenshiLib.LOGGER.error("Error reading file {}", fileName);
        throw new FileNotFoundException();
    }

    public static InputStream getDataStream(String modid, String dir, String fileName) throws FileNotFoundException {
        InputStream input = ResourceStream.class.getResourceAsStream("/data/" + modid + "/" + dir + "/" + fileName);
        if (input != null)
            return input;
        TenshiLib.LOGGER.error("Error reading file {}", fileName);
        throw new FileNotFoundException();
    }
}
