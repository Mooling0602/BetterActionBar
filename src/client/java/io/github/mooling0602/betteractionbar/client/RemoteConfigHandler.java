package io.github.mooling0602.betteractionbar.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public final class RemoteConfigHandler {

    private static final Gson GSON = new Gson();
    private static final String CONFIG_KEY = "betteractionbar";

    private RemoteConfigHandler() {}

    public static void handleConfigText(String hoverText) {
        JsonObject config = parseConfig(hoverText);
        if (config == null) {
            return;
        }
        BetterActionBarConfig.applyRemoteConfig(config);
        BetterActionBarClient.LOG.info(
            "Applied remote config from chat hoverEvent"
        );
    }

    private static JsonObject parseConfig(String text) {
        JsonObject config = tryParseJson(text);
        if (config != null) {
            return config;
        }
        try {
            return tryParseJson(pythonDictToJson(text));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject tryParseJson(String text) {
        try {
            JsonElement element = GSON.fromJson(text, JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                return null;
            }
            JsonObject root = element.getAsJsonObject();
            JsonElement configElement = BetterActionBarConfig.getMemberIgnoreCase(
                root,
                CONFIG_KEY
            );
            if (configElement == null || !configElement.isJsonObject()) {
                return null;
            }
            return configElement.getAsJsonObject();
        } catch (JsonParseException ignored) {
            return null;
        }
    }

    private static String pythonDictToJson(String text) {
        StringBuilder json = new StringBuilder(text.length());
        boolean inString = false;
        char stringChar = '"';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';

            if (!inString) {
                if (c == '\'' || c == '"') {
                    inString = true;
                    stringChar = c;
                    json.append('"');
                } else if (c == 'N' && text.startsWith("None", i)) {
                    json.append("null");
                    i += 3;
                } else if (c == 'T' && text.startsWith("True", i)) {
                    json.append("true");
                    i += 3;
                } else if (c == 'F' && text.startsWith("False", i)) {
                    json.append("false");
                    i += 4;
                } else {
                    json.append(c);
                }
            } else {
                if (c == '\\' && next != '\0') {
                    json.append(c);
                    json.append(next);
                    i++;
                } else if (c == stringChar) {
                    inString = false;
                    json.append('"');
                } else {
                    json.append(c);
                }
            }
        }
        return json.toString();
    }
}
