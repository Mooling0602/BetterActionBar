package io.github.mooling0602.betteractionbar.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.mooling0602.betteractionbar.BetterActionBarMod;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class BetterActionBarConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(BetterActionBarMod.MOD_ID + ".json");

	private static BetterActionBarConfig config = BetterActionBarConfig.createDefault();

	private BetterActionBarConfigManager() {
	}

	public static void load() {
		BetterActionBarConfig loaded = BetterActionBarConfig.createDefault();
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
				BetterActionBarConfig parsed = GSON.fromJson(reader, BetterActionBarConfig.class);
				if (parsed != null) {
					loaded = parsed;
				}
			} catch (IOException | RuntimeException ignored) {
				loaded = BetterActionBarConfig.createDefault();
			}
		}

		loaded.sanitize();
		config = loaded;
		saveCurrent();
	}

	public static BetterActionBarConfig getConfig() {
		return config;
	}

	public static BetterActionBarConfig copyOf() {
		return config.copy();
	}

	public static void save(BetterActionBarConfig newConfig) {
		config = newConfig.copy();
		config.sanitize();
		saveCurrent();
	}

	private static void saveCurrent() {
		try {
			Path parent = CONFIG_PATH.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException ignored) {
		}
	}
}
