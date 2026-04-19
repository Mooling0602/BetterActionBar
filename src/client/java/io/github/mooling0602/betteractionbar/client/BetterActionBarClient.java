package io.github.mooling0602.betteractionbar.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterActionBarClient implements ClientModInitializer {

    public static final Logger LOG = LoggerFactory.getLogger("BetterActionBar");

    @Override
    public void onInitializeClient() {
        BetterActionBarConfig.reload();
    }
}
