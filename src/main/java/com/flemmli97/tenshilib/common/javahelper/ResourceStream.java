package com.flemmli97.tenshilib.common.javahelper;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.flemmli97.tenshilib.TenshiLib;

import net.minecraftforge.fml.common.Loader;

public class ResourceStream {

    public static InputStream getStream(String modid, String dir, String fileName) throws FileNotFoundException {
        InputStream input = Loader.class.getResourceAsStream("/assets/" + modid + "/" + dir + "/" + fileName);
        if(input != null)
            return input;
        TenshiLib.logger.error("Error reading file {}", fileName);
        throw new FileNotFoundException();
    }
}
