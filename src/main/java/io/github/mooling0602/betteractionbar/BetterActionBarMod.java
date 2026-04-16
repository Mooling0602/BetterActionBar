package io.github.mooling0602.betteractionbar;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterActionBarMod implements ModInitializer {

    public static final String MOD_ID = "betteractionbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BetterActionBar initialized.");
    }
}
