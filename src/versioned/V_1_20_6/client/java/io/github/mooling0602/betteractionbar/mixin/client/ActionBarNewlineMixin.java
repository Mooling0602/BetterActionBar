package io.github.mooling0602.betteractionbar.mixin.client;

import java.util.List;
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
	private static final String NEWLINE = "\n";
	private static final int OVERLAY_FADE_TICKS = 20;
	private static final int MIN_VISIBLE_ALPHA = 8;
	private static final int DEFAULT_TEXT_COLOR_RGB = 0xFFFFFF;
	private static final float RAINBOW_HUE_CYCLE_TICKS = 50.0F;
	private static final float RAINBOW_SATURATION = 0.7F;
	private static final float RAINBOW_BRIGHTNESS = 0.6F;
	private static final int SPLIT_WIDTH_UNLIMITED = 1_000_000;
	private static final int BASE_LINE_HEIGHT = 9;
	private static final float EXTRA_LINE_SPACING_RATIO = 0.1F;
	private static final int BASELINE_OFFSET_FROM_BOTTOM = 68;

	@Shadow @Final private Minecraft minecraft;
	@Shadow @Nullable private Component overlayMessageString;
	@Shadow private int overlayMessageTime;
	@Shadow private boolean animateOverlayMessageColor;

	@Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
	private void betterActionBar$renderMultilineOverlay(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
		if (!this.betterActionBar$shouldHandleMultilineOverlay()) {
			return;
		}

		int alpha = this.betterActionBar$calculateOverlayAlpha();
		if (alpha <= MIN_VISIBLE_ALPHA) {
			// Vanilla skips drawing when alpha is nearly transparent; cancel to avoid duplicate single-line render.
			ci.cancel();
			return;
		}

		int drawColor = this.betterActionBar$buildDrawColor(alpha);
		List<FormattedCharSequence> lines = this.betterActionBar$splitOverlayLines();
		this.betterActionBar$drawCenteredLines(guiGraphics, lines, drawColor);

		// We fully replace vanilla overlay rendering only for messages containing explicit newlines.
		ci.cancel();
	}

	private boolean betterActionBar$shouldHandleMultilineOverlay() {
		if (this.overlayMessageString == null || this.overlayMessageTime <= 0) {
			return false;
		}

		// Keep vanilla path for regular single-line messages.
		return this.overlayMessageString.getString().contains(NEWLINE);
	}

	private int betterActionBar$calculateOverlayAlpha() {
		return Mth.clamp(this.overlayMessageTime * 255 / OVERLAY_FADE_TICKS, 0, 255);
	}

	private int betterActionBar$buildDrawColor(int alpha) {
		int color = DEFAULT_TEXT_COLOR_RGB;
		if (this.animateOverlayMessageColor) {
			color = Mth.hsvToRgb((float) this.overlayMessageTime / RAINBOW_HUE_CYCLE_TICKS, RAINBOW_SATURATION, RAINBOW_BRIGHTNESS) & DEFAULT_TEXT_COLOR_RGB;
		}
		return color | (alpha << 24);
	}

	private List<FormattedCharSequence> betterActionBar$splitOverlayLines() {
		return this.minecraft.font.split(this.overlayMessageString, SPLIT_WIDTH_UNLIMITED);
	}

	private void betterActionBar$drawCenteredLines(GuiGraphics guiGraphics, List<FormattedCharSequence> lines, int drawColor) {
		int lineStep = this.betterActionBar$getLineStep();
		int firstLineY = this.betterActionBar$calculateFirstLineY(guiGraphics.guiHeight(), lines.size());
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			int x = this.betterActionBar$calculateCenteredX(guiGraphics.guiWidth(), line);
			int y = firstLineY + i * lineStep;
			guiGraphics.drawString(this.minecraft.font, line, x, y, drawColor, true);
		}
	}

	private int betterActionBar$calculateFirstLineY(int guiHeight, int lineCount) {
		int lineStep = this.betterActionBar$getLineStep();
		int baseY = guiHeight - BASELINE_OFFSET_FROM_BOTTOM;
		return baseY - (lineCount - 1) * lineStep;
	}

	private int betterActionBar$getLineStep() {
		return Math.round(BASE_LINE_HEIGHT * (1.0F + EXTRA_LINE_SPACING_RATIO));
	}

	private int betterActionBar$calculateCenteredX(int guiWidth, FormattedCharSequence line) {
		return (guiWidth - this.minecraft.font.width(line)) / 2;
	}
}
