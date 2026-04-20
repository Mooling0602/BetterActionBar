package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.client.BetterActionBarClient;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsScreenMixin {

  @Inject(method = "addOptions", at = @At("HEAD"), cancellable = true)
  private void addOptionsForBetterActionBar(CallbackInfo ci) {
    ci.cancel();
    AccessibilityOptionsScreen screen = (AccessibilityOptionsScreen) (Object) this;
    OptionsList list = ((OptionsSubScreenAccessor) screen).getList();
    OptionInstance<?>[] activeOptions = getOptions(screen);
    BetterActionBarClient.LOG.info(
        "Active options: " + activeOptions.length);
    Button BetterActionBarConfigEntry = Button.builder(
        Component.translatable("betteractionbar.config.entry"),
        btn -> {
          BetterActionBarClient.LOG.info(
              "Clicked BetterActionBarEntry button."); // Open Config UI screen in further impl.
        })
        .width(150)
        .build();
    for (int i = 0; i < activeOptions.length - 1; i += 2) {
      list.addSmall(activeOptions[i], activeOptions[i + 1]);
    }
    if (activeOptions.length > 0) {
      int lastIndex = activeOptions.length - 1;
      AbstractWidget lastWidget = activeOptions[lastIndex].createButton(
          ((OptionsSubScreenAccessor) screen).getOptions());
      list.addSmall(lastWidget, BetterActionBarConfigEntry);
    }
  }

  private OptionInstance<?>[] getOptions(AccessibilityOptionsScreen screen) {
    try {
      java.lang.reflect.Method method = AccessibilityOptionsScreen.class.getDeclaredMethod(
          "options",
          net.minecraft.client.Options.class);
      method.setAccessible(true);
      return (OptionInstance<?>[]) method.invoke(
          screen,
          ((OptionsSubScreenAccessor) screen).getOptions());
    } catch (Exception e) {
      BetterActionBarClient.LOG.error("Failed to get options", e);
      return new OptionInstance[0];
    }
  }
}
