package io.github.mooling0602.betteractionbar.mixin.client;

import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ActionBarNewlineMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Nullable private Component overlayMessageString;
	@Shadow private int overlayMessageTime;
	@Shadow private boolean animateOverlayMessageColor;

	@Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
	private void betterActionBar$renderMultilineOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (this.overlayMessageString == null || this.overlayMessageTime <= 0) {
			return;
		}

		if (!this.overlayMessageString.getString().contains("\n")) {
			return;
		}

		int alpha = Mth.clamp(this.overlayMessageTime * 255 / 20, 0, 255);
		if (alpha <= 8) {
			ci.cancel();
			return;
		}

		int color = 0xFFFFFF;
		if (this.animateOverlayMessageColor) {
			color = Mth.hsvToRgb((float) this.overlayMessageTime / 50.0F, 0.7F, 0.6F) & 0xFFFFFF;
		}

		List<FormattedCharSequence> lines = this.minecraft.font.split(this.overlayMessageString, 1_000_000);
		int lineHeight = 9;
		int baseY = guiGraphics.guiHeight() - 68;
		int firstLineY = baseY - (lines.size() - 1) * lineHeight;
		int drawColor = color | (alpha << 24);

		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			int x = (guiGraphics.guiWidth() - this.minecraft.font.width(line)) / 2;
			guiGraphics.drawString(this.minecraft.font, line, x, firstLineY + i * lineHeight, drawColor, true);
		}

		ci.cancel();
	}
}
