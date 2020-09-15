package com.flemmli97.tenshilib.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class JsonModelHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Creates a generic json file for item models from the specifide mod
     * 
     * @param assetsFolder The folder where the json models end up at
     */
    public static void itemJson(File assetsFolder, Iterable<Item> list) {
        try {
            for (Item item : list) {
                File jsonFile = new File(assetsFolder, item.getRegistryName().getPath() + ".json");
                if (!jsonFile.exists()) {
                    jsonFile.createNewFile();
                    FileWriter writer = new FileWriter(jsonFile);
                    JsonWriter json = GSON.newJsonWriter(writer);
                    json.beginObject();
                    json.name("parent");
                    json.value("minecraft:item/generated");
                    json.name("textures");
                    json.beginObject();
                    json.name("layer0");
                    json.value(item.getRegistryName().getNamespace()+":items/" + item.getRegistryName().getPath());
                    json.endObject();
                    json.endObject();
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void blockJson(File assetsFolder, Iterable<Block> list, @Nullable Function<Block, String> parentModel) {
        try {
            new File(assetsFolder+"/blockstates").mkdir();
            new File(assetsFolder+"/model").mkdir();
            new File(assetsFolder+"/item").mkdir();
            for (Block block : list) {
                File jsonFile = new File(assetsFolder+"/blockstates", block.getRegistryName().getPath() + ".json");
                if (!jsonFile.exists()) {
                    jsonFile.createNewFile();
                    FileWriter writer = new FileWriter(jsonFile);
                    JsonWriter json = GSON.newJsonWriter(writer);
                    json.beginObject();
                    json.name("variants");
                    json.beginObject();
                    json.name("normal");
                    json.beginObject();
                    json.name("model");
                    json.value(block.getRegistryName().toString());
                    json.endObject();
                    json.endObject();
                    json.endObject();
                    writer.close();
                }

                File jsonFile2 = new File(assetsFolder+"/model", block.getRegistryName().getPath() + ".json");
                if (!jsonFile2.exists()) {
                    jsonFile2.createNewFile();
                    FileWriter writer = new FileWriter(jsonFile2);
                    JsonWriter json = GSON.newJsonWriter(writer);
                    json.beginObject();
                    json.name("parent");
                    json.value(parentModel.apply(block));
                    json.name("textures");
                    json.beginObject();
                    json.name("0");
                    json.value(block.getRegistryName().getNamespace()+":blocks/"+ block.getRegistryName().getPath());
                    json.endObject();
                    json.endObject();
                    writer.close();
                }
                File jsonFile3 = new File(assetsFolder+"/item", block.getRegistryName().getPath() + ".json");
                if (!jsonFile3.exists()) {
                    jsonFile3.createNewFile();
                    FileWriter writer = new FileWriter(jsonFile3);
                    JsonWriter json = GSON.newJsonWriter(writer);
                    json.beginObject();
                    json.name("parent");
                    json.value(block.getRegistryName().getNamespace()+":block/" + block.getRegistryName().getPath());
                    json.endObject();
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lang(File assetsFolder, List<Item> list) {
        try {
            File jsonFile = new File(assetsFolder, "rawItemLang.json");
            if (!jsonFile.exists()) {
                FileWriter writer = new FileWriter(jsonFile);
                BufferedWriter buf = new BufferedWriter(writer);
                for (Item item : list) {
                    buf.write(item.getRegistryName() + ".name=");
                    buf.newLine();
                }
                buf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
