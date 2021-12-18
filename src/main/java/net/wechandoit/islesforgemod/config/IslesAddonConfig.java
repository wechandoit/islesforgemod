package net.wechandoit.islesforgemod.config;

import com.google.common.base.Preconditions;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.IteratableOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.wechandoit.islesforgemod.Islesforgemod;
import net.wechandoit.islesforgemod.structure.Config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class IslesAddonConfig {

    public static final Config CONFIG;

    public static final String FILE_NAME = "islesaddons.json";

    static {
        InputStream def = Islesforgemod.class.getResourceAsStream("/islesaddonconfig.json");
        CONFIG = new Config(def);
    }

    static AbstractOption[] getOptions() {
        return CONFIG.entrySet().stream()
                .map(IslesAddonConfig::toOption)
                .filter(Objects::nonNull)
                .toArray(x$0 -> new AbstractOption[x$0]);
    }

    public static boolean doesNotExist() {
        return Files.notExists(getConfigDir().resolve(FILE_NAME), new java.nio.file.LinkOption[0]);
    }

    public static void load() {
        Path path = getConfigDir().resolve(FILE_NAME);
        if (Files.exists(path, new java.nio.file.LinkOption[0])) {
            CONFIG.load(path);
        } else {
            save();
        }
    }

    public static void save() {
        CONFIG.save(getConfigDir().resolve(FILE_NAME));
    }

    private static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private static AbstractOption toOption(Map.Entry<String, Object> entry) {
        AbstractOption option = null;
        String key = entry.getKey();
        if (key.endsWith(".min") || key.endsWith(".max") || key.endsWith(".step"))
            return null;
        Object value = entry.getValue();
        String translationKey = "option.isles-addons." + key;
        if (value instanceof Number) {
            Number min = CONFIG.get(key + ".min", Number.class);
            Number max = CONFIG.get(key + ".max", Number.class);
            Number step = CONFIG.get(key + ".step", Number.class);
            option = new SliderPercentageOption(translationKey, min.doubleValue(), max.doubleValue(), step.floatValue(), __ -> Double.valueOf(((Number)CONFIG.get(key, Number.class)).doubleValue()), (__, v) -> CONFIG.put(key, v), (__, ___) -> new TranslationTextComponent(translationKey));
        } else if (value instanceof Boolean) {
            option = new BooleanOption(translationKey, __ -> ((Boolean)CONFIG.get(key, Boolean.class)).booleanValue(), (__, v) -> CONFIG.put(key, v));
        } else if (value instanceof Collection) {
            List<String> options = (List<String>)((Collection)value).stream().map(String::valueOf).collect(Collectors.toList());
            Preconditions.checkState(!options.isEmpty(), "no options: " + key);
            option = new IteratableOption(translationKey, (__, a) -> {
                int amount = a.intValue();
                for (int i = 0; i < amount; i++)
                    options.add(options.remove(0));
            }, (__, o) -> new TranslationTextComponent("option.isles-addons." + options.get(0)));
        } else {
            throw new IllegalStateException();
        }
        String tooltipKey = "option.isles-addons." + key + ".tooltip";
        if (I18n.hasKey(tooltipKey))
            option.setOptionValues((Minecraft.getInstance()).fontRenderer.trimStringToWidth(new TranslationTextComponent(tooltipKey), 200));
        return option;
    }
}
