package com.flemmli97.tenshilib.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.flemmli97.tenshilib.TenshiLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Less restrictive config based on forges existing config system.
 * Reloading and syncing across sides needs to be done manually.
 */
public class Configuration<T> {

    private final ForgeConfigSpec spec;
    private final T confHolder;
    private final CommentedFileConfig config;
    private final String modid;
    private final Consumer<T> load;

    public Configuration(Function<ForgeConfigSpec.Builder, T> func, Function<Path, Path> resolvePath, Consumer<T> load, String modid) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        this.confHolder = func.apply(builder);
        this.spec = builder.build();
        this.modid = modid;
        this.load = load;
        File configFile = resolvePath.apply(FMLPaths.CONFIGDIR.get()).toFile();
        this.config = CommentedFileConfig.builder(configFile).sync().
                preserveInsertionOrder().
                autosave().
                onFileNotFound((newfile, configFormat) -> {
                    Files.createFile(newfile);
                    configFormat.initEmptyFile(newfile);
                    return true;
                }).
                writingMode(WritingMode.REPLACE).
                build();
        this.spec.setConfig(this.config);
    }

    public void loadConfig() {
        try {
            if (!this.spec.isCorrect(this.config))
                this.spec.correct(this.config);
            this.config.load();
            this.load.accept(this.confHolder);
        } catch (ParsingException ex) {
            TenshiLib.logger.error("Failed loading config file " + this.config.getNioPath().toString() + " for mod " + this.modid + "{}", ex.getCause());
        }
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }

    public T getConfHolder() {
        return this.confHolder;
    }
}
