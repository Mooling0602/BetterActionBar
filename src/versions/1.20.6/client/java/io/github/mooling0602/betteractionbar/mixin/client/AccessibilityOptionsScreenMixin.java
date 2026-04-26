package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.client.BetterActionBarClient;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin extends SimpleOptionsSubScreen {

    /*
     * Mixin constructor matching SimpleOptionsSubScreen.
     * Never called at runtime — Mixin injects methods directly into the target.
     */
    protected AccessibilityOptionsScreenMixin(
        Screen lastScreen,
        Options options,
        Component title,
        OptionInstance<?>[] smallOptions
    ) {
        super(lastScreen, options, title, smallOptions);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addBetterActionBarConfigButton(CallbackInfo ci) {
        BetterActionBarClient.LOG.info(
            "Active options: " + this.smallOptions.length
        );
        Button configEntry = Button.builder(
            Component.translatable("betteractionbar.config.entry"),
            btn -> BetterActionBarClient.LOG.info(
                "Clicked BetterActionBarEntry button."
            )
        ).width(150).build();
        this.list.addSmall(configEntry, null);
    }
}
