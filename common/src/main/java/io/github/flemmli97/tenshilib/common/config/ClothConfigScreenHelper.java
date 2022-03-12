package io.github.flemmli97.tenshilib.common.config;

import io.github.flemmli97.tenshilib.TenshiLib;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ClothConfigScreenHelper {

    /**
     * Create a new screen for configs with the help of cloth config api
     *
     * @return Config screen to display to the user
     */
    public static Screen configScreenOf(Screen current, String modid, List<JsonConfig<CommentedJsonConfig>> confs) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(current)
                .setTitle(new TranslatableComponent(String.format("config.title.%s", modid)));
        if (confs.size() == 1) {
            JsonConfig<CommentedJsonConfig> conf = confs.get(0);
            conf.getElement().getConfigVals().forEach((key, val) -> {
                String id = category(key);
                ConfigCategory cat = builder.getOrCreateCategory(ofKey(key));
                if (!id.isEmpty() && conf.getElement().categoryComments.containsKey(id)) {
                    cat.setDescription(conf.getElement().categoryComments.get(id).stream()
                            .map(TextComponent::new).toList().toArray(new FormattedText[0]));
                }
                cat.addEntry(ofVal(key, val));
            });
            return builder.build();
        }
        for (JsonConfig<CommentedJsonConfig> conf : confs) {
            ConfigCategory cat = builder.getOrCreateCategory(new TranslatableComponent(String.format("%s", conf.getName())));
            Map<Component, SubCategoryBuilder> subCats = new LinkedHashMap<>();
            conf.getElement().getConfigVals().forEach((key, val) -> {
                AbstractConfigListEntry<?> entry = ofVal(key, val);

                SubCategoryBuilder sub = subCats.computeIfAbsent(ofKey(key), c -> ConfigEntryBuilder.create().startSubCategory(c));
                String id = category(key);
                if (!id.isEmpty() && conf.getElement().categoryComments.containsKey(id)) {
                    sub.setTooltip(conf.getElement().categoryComments.get(id).stream()
                            .map(TextComponent::new).toList().toArray(new Component[0]));
                }
                if (entry != null)
                    sub.add(entry);
            });
            if (subCats.values().size() == 1)
                subCats.values().forEach(sub -> sub.stream().iterator().forEachRemaining(cat::addEntry));
            else
                subCats.values().forEach(sub -> cat.addEntry(sub.build()));
        }
        builder.setSavingRunnable(() -> confs.forEach(jcfg -> {
            jcfg.save();
            jcfg.getElement().onReload();
        }));
        return builder.build();
    }

    private static Component ofKey(String key) {
        String cat = category(key);
        if (!cat.isEmpty()) {
            return new TranslatableComponent(String.format("category.%s", cat));
        }
        return new TranslatableComponent("category.general");
    }

    private static String category(String key) {
        if (key.contains(".")) {
            int i = key.lastIndexOf(".");
            return key.substring(0, i);
        }
        return "";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static AbstractConfigListEntry<?> ofVal(String key, CommentedJsonConfig.CommentedVal<?> val) {
        int i = key.indexOf(".");
        if (i >= 0 && i + 1 < key.length())
            key = key.substring(i + 1);
        ConfigEntryBuilder builder = ConfigEntryBuilder.create();
        if (val instanceof CommentedJsonConfig.IntVal intVal) {
            IntFieldBuilder intFieldBuilder = builder.startIntField(new TranslatableComponent(String.format("%s", key)), intVal.input)
                    .setDefaultValue(intVal.defaultVal)
                    .setTooltip(ofComments(val))
                    .setSaveConsumer(intVal::set)
                    .setMax(intVal.max)
                    .setMin(intVal.min);
            return intFieldBuilder.build();
        } else if (val instanceof CommentedJsonConfig.DoubleVal doubleVal) {
            DoubleFieldBuilder doubleFieldBuilder = builder.startDoubleField(new TranslatableComponent(String.format("%s", key)), doubleVal.input)
                    .setDefaultValue(doubleVal.defaultVal)
                    .setTooltip(ofComments(val))
                    .setSaveConsumer(doubleVal::set)
                    .setMax(doubleVal.max)
                    .setMin(doubleVal.min);
            return doubleFieldBuilder.build();
        } else if (val.input instanceof Enum<?>) {
            CommentedJsonConfig.CommentedVal<Enum<?>> cv = (CommentedJsonConfig.CommentedVal<Enum<?>>) val;
            EnumSelectorBuilder<?> enumFieldBuilder = builder.startEnumSelector(new TranslatableComponent(String.format("%s", key)), ((Enum) cv.input).getDeclaringClass(), cv.input)
                    .setDefaultValue(() -> cv.defaultVal)
                    .setTooltip(ofComments(val))
                    .setEnumNameProvider(enumProvider());
            enumFieldBuilder.setSaveConsumer(cv::set);
            return enumFieldBuilder.build();
        } else if (val instanceof CommentedJsonConfig.ListVal listVal) {
            if (listVal.validator.test("s")) {
                return builder.startStrList(new TranslatableComponent(String.format("%s", key)), ((CommentedJsonConfig.ListVal<String>) listVal).input)
                        .setDefaultValue(((CommentedJsonConfig.ListVal<String>) listVal).defaultVal).setSaveConsumer(((CommentedJsonConfig.ListVal<String>) listVal)::set)
                        .setTooltip(ofComments(val)).build();
            }
            if (listVal.validator.test(0)) {
                return builder.startIntList(new TranslatableComponent(String.format("%s", key)), ((CommentedJsonConfig.ListVal<Integer>) listVal).input)
                        .setDefaultValue(((CommentedJsonConfig.ListVal<Integer>) listVal).defaultVal).setSaveConsumer(((CommentedJsonConfig.ListVal<Integer>) listVal)::set)
                        .setTooltip(ofComments(val)).build();
            }
            if (listVal.validator.test(0.0)) {
                return builder.startDoubleList(new TranslatableComponent(String.format("%s", key)), ((CommentedJsonConfig.ListVal<Double>) listVal).input)
                        .setDefaultValue(((CommentedJsonConfig.ListVal<Double>) listVal).defaultVal).setSaveConsumer(((CommentedJsonConfig.ListVal<Double>) listVal)::set)
                        .setTooltip(ofComments(val)).build();
            }
        } else if (val.input instanceof String) {
            CommentedJsonConfig.CommentedVal<String> sv = (CommentedJsonConfig.CommentedVal<String>) val;
            return builder.startStrField(new TranslatableComponent(String.format("%s", key)), sv.input)
                    .setDefaultValue(sv.defaultVal)
                    .setTooltip(ofComments(val))
                    .setSaveConsumer(sv::set).build();
        } else if (val.input instanceof Boolean) {
            CommentedJsonConfig.CommentedVal<Boolean> bv = (CommentedJsonConfig.CommentedVal<Boolean>) val;
            return builder.startBooleanToggle(new TranslatableComponent(String.format("%s", key)), bv.input)
                    .setDefaultValue(bv.defaultVal)
                    .setTooltip(ofComments(val))
                    .setSaveConsumer(bv::set).build();
        }
        TenshiLib.logger.error("Unsupported config value type for " + key + " val " + val.input);
        return null;
    }

    private static <T extends Enum<?>> Function<T, Component> enumProvider() {
        return v -> new TranslatableComponent(v.name());
    }

    private static final int stringLength = 55;

    private static Component[] ofComments(CommentedJsonConfig.CommentedVal<?> val) {
        List<Component> wrapped = new ArrayList<>();
        for (String s : val.__comments) {
            while (s.length() >= stringLength) {
                String st = s.substring(0, stringLength);
                int whiteSpace = st.lastIndexOf(" ");
                if(whiteSpace < 0)
                    whiteSpace = st.length();
                wrapped.add(new TextComponent(st.substring(0, whiteSpace)));
                s = s.substring(stringLength);
                if(whiteSpace + 1 < st.length())
                    s = st.substring(whiteSpace+1) + s;
            }
            wrapped.add(new TextComponent(s));
        }
        return wrapped.toArray(new Component[0]);
    }
}
