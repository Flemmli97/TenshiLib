package io.github.flemmli97.tenshilib.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.flemmli97.tenshilib.TenshiLib;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class JsonConfig<T> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Gson GSONCommentSaver = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path file;
    private T element;
    private final Class<T> type;
    private boolean mcRestart, worldRestart;
    private String name;
    private Gson gson = GSON;

    public JsonConfig(Path file, Class<T> type, @Nullable T defaultValue) {
        this.file = file;
        this.type = type;
        this.name = this.file.getFileName().toString();
        this.element = defaultValue;
        if (!Files.exists(file)) {
            file.getParent().toFile().mkdirs();
            try {
                Files.createFile(file);
                if (this.element != null) {
                    this.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.load();
    }

    public JsonConfig(Path file, Class<T> type, @Nullable Path defaultConfig) {
        this.file = file;
        this.type = type;
        this.name = this.file.getFileName().toString();
        if (!Files.exists(file)) {
            file.getParent().toFile().mkdirs();
            try {
                Files.createFile(file);
                if (defaultConfig != null && Files.exists(defaultConfig)) {
                    Files.copy(defaultConfig, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.load();
    }

    public JsonConfig<T> setGson(Gson gson) {
        this.gson = gson;
        this.load();
        return this;
    }

    public Path getConfigPath() {
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
            FileReader reader = new FileReader(this.file.toFile());
            if (this.element instanceof CommentedJsonConfig conf) {
                Set<String> faulty = conf.deserialize(this.gson.fromJson(reader, JsonObject.class), this.gson);
                if (!faulty.isEmpty())
                    throw new JsonSyntaxException("Faulty keys " + faulty);
            } else
                this.element = this.gson.fromJson(reader, this.type);
            reader.close();
        } catch (IllegalStateException | JsonSyntaxException e) {
            try {
                TenshiLib.logger.error("Json config doesn't match expected config. Creating a backup. This is probably caused either by a config update or malformed json.");
                e.printStackTrace();
                int back = 0;
                String file = this.file.getFileName().toString() + "_back";
                while (Files.exists(this.file.getParent().resolve(file))) {
                    back++;
                    file = file + back;
                }
                Files.copy(this.file, this.file.getParent().resolve(file));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.save();
    }

    public void save() {
        try {
            FileWriter writer = new FileWriter(this.file.toFile());
            if (this.element instanceof CommentedJsonConfig conf)
                this.gson.toJson(conf.serialize(this.gson), writer);
            else
                this.gson.toJson(this.element, writer);
            writer.close();
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }
}
