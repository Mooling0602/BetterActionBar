package io.github.mooling0602.betteractionbar.client;

import net.fabricmc.api.ClientModInitializer;

public class BetterActionBarClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Intentionally empty: this mod is driven by mixins only.
		// Keeping a client entrypoint is still useful for explicit client-only wiring in fabric.mod.json.
	}
}
