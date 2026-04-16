package io.github.mooling0602.betteractionbar.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mooling0602.betteractionbar.BetterActionBarMod;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class BetterActionBarConfig {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private static final String CONFIG_FILE_NAME =
        BetterActionBarMod.MOD_ID + ".json";
    private static final float DEFAULT_LINE_SPACING_MULTIPLIER = 0.1F;
    private static final List<String> DEFAULT_NEW_LINE_BREAKS = List.of();

    private static volatile BetterActionBarConfig instance = load();

    private final float lineSpacingMultiplier;
    private final List<String> newLineBreaks;

    private BetterActionBarConfig(
        float lineSpacingMultiplier,
        List<String> newLineBreaks
    ) {
        this.lineSpacingMultiplier = lineSpacingMultiplier;
        this.newLineBreaks = Collections.unmodifiableList(newLineBreaks);
    }

    public static BetterActionBarConfig get() {
        return instance;
    }

    public static void reload() {
        instance = load();
    }

    public float lineSpacingMultiplier() {
        return this.lineSpacingMultiplier;
    }

    public List<String> newLineBreaks() {
        return this.newLineBreaks;
    }

    public static String normalizeNewLineBreaks(String text) {
        String normalized = text;
        for (String breakToken : get().newLineBreaks()) {
            if (breakToken == null || breakToken.isEmpty()) {
                continue;
            }
            normalized = normalized.replace(breakToken, "\n");
        }
        return normalized.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static BetterActionBarConfig load() {
        Path configFile = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configFile)) {
            BetterActionBarConfig config = defaultConfig();
            write(configFile, config);
            return config;
        }

        try (
            Reader reader = Files.newBufferedReader(
                configFile,
                StandardCharsets.UTF_8
            )
        ) {
            JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                return defaultConfig();
            }
            return fromJson(jsonElement.getAsJsonObject());
        } catch (IOException | JsonParseException e) {
            BetterActionBarMod.LOGGER.warn(
                "Failed to load BetterActionBar config from {}. Falling back to defaults.",
                configFile,
                e
            );
            return defaultConfig();
        }
    }

    private static BetterActionBarConfig fromJson(JsonObject jsonObject) {
        float lineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER;
        JsonElement lineSpacingElement = getMemberIgnoreCase(
            jsonObject,
            "LineSpacingMultiplier"
        );
        if (lineSpacingElement != null) {
            try {
                lineSpacingMultiplier = lineSpacingElement.getAsFloat();
            } catch (RuntimeException ignored) {
                lineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER;
            }
        }

        List<String> newLineBreaks = new ArrayList<>();
        JsonElement newLineBreakElement = getMemberIgnoreCase(
            jsonObject,
            "NewLineBreak"
        );
        if (newLineBreakElement != null && newLineBreakElement.isJsonArray()) {
            JsonArray jsonArray = newLineBreakElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element != null && element.isJsonPrimitive()) {
                    String value = element.getAsString();
                    if (!value.isEmpty()) {
                        newLineBreaks.add(value);
                    }
                }
            }
        }

        return new BetterActionBarConfig(lineSpacingMultiplier, newLineBreaks);
    }

    private static BetterActionBarConfig defaultConfig() {
        return new BetterActionBarConfig(
            DEFAULT_LINE_SPACING_MULTIPLIER,
            new ArrayList<>(DEFAULT_NEW_LINE_BREAKS)
        );
    }

    private static JsonElement getMemberIgnoreCase(
        JsonObject jsonObject,
        String key
    ) {
        if (jsonObject.has(key)) {
            return jsonObject.get(key);
        }

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static void write(Path configFile, BetterActionBarConfig config) {
        try {
            Path parent = configFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (
                Writer writer = Files.newBufferedWriter(
                    configFile,
                    StandardCharsets.UTF_8
                )
            ) {
                GSON.toJson(toJson(config), writer);
            }
        } catch (IOException e) {
            BetterActionBarMod.LOGGER.warn(
                "Failed to write default BetterActionBar config to {}.",
                configFile,
                e
            );
        }
    }

    private static JsonObject toJson(BetterActionBarConfig config) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(
            "LineSpacingMultiplier",
            config.lineSpacingMultiplier()
        );
        JsonArray jsonArray = new JsonArray();
        for (String breakToken : config.newLineBreaks()) {
            jsonArray.add(breakToken);
        }
        jsonObject.add("NewLineBreak", jsonArray);
        return jsonObject;
    }
}
