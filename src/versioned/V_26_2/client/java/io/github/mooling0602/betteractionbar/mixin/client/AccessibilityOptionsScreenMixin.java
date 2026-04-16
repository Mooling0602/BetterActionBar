package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.client.BetterActionBarConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin {
	@Shadow @Final protected MinecraftClient client;
	@Shadow protected OptionListWidget body;

	@Inject(method = "init", at = @At("TAIL"))
	private void betterActionBar$addConfigButton(CallbackInfo ci) {
		ClickableWidget spacer = ButtonWidget.builder(Text.empty(), button -> { })
			.dimensions(0, 0, 0, 0)
			.build();
		ClickableWidget button = ButtonWidget.builder(Text.translatable("screen.betteractionbar.actionbar_font_style"), press ->
			this.client.setScreenAndRender(new BetterActionBarConfigScreen((Screen) (Object) this)))
			.dimensions(0, 0, this.body.getRowWidth(), 20)
			.build();
		this.body.addWidgetEntry(spacer, button);
	}
}
