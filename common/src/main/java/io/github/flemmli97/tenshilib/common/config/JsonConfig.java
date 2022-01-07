package io.github.flemmli97.tenshilib.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonConfig<T> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final File file;
    private T element;
    private final Class<T> type;
    private boolean mcRestart, worldRestart;
    private String name;
    private Gson gson = GSON;

    public JsonConfig(File file, Class<T> type, @Nullable T defaultValue) {
        this.file = file;
        this.type = type;
        this.name = this.file.getName();
        if (!this.file.getParentFile().exists())
            this.file.getParentFile().mkdirs();
        if (!this.file.exists())
            try {
                this.file.createNewFile();
                if (defaultValue != null) {
                    this.element = defaultValue;
                    this.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        this.load();
    }

    public JsonConfig(File file, Class<T> type, @Nullable File defaultConfig) {
        this.file = file;
        this.type = type;
        this.name = this.file.getName();
        if (!this.file.getParentFile().exists())
            this.file.getParentFile().mkdirs();
        if (!this.file.exists())
            try {
                this.file.createNewFile();
                if (defaultConfig != null && defaultConfig.exists()) {
                    InputStream in = new FileInputStream(defaultConfig);
                    OutputStream out = new FileOutputStream(this.file);
                    IOUtils.copy(in, out);
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        this.load();
    }

    public JsonConfig<T> setGson(Gson gson) {
        this.gson = gson;
        this.load();
        return this;
    }

    public File getConfigFile() {
        return this.file;
    }

    public T getElement() {
        return this.element;
    }

    public JsonConfig<T> setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public void setElement(T element) {
        this.element = element;
    }

    public JsonConfig<T> setMCRestart(boolean flag) {
        this.mcRestart = flag;
        return this;
    }

    public JsonConfig<T> setWorldRestart(boolean flag) {
        this.worldRestart = flag;
        return this;
    }

    public boolean mcRestart() {
        return this.mcRestart;
    }

    public boolean worldRestart() {
        return this.worldRestart;
    }

    public void load() {
        try {
            FileReader reader = new FileReader(this.file);
            this.element = this.gson.fromJson(reader, this.type);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(this.file);
            this.gson.toJson(this.element, writer);
            writer.close();
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }
}
