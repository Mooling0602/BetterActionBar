package io.github.mooling0602.betteractionbar.mixin.client;

import java.util.List;
import io.github.mooling0602.betteractionbar.client.ActionBarOverlaySupport;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
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

	@Inject(method = "extractOverlayMessage", at = @At("HEAD"), cancellable = true)
	private void betterActionBar$renderMultilineOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!this.betterActionBar$shouldHandleMultilineOverlay()) {
			return;
		}

		int alpha = ActionBarOverlaySupport.calculateOverlayAlpha(this.overlayMessageTime);
		if (ActionBarOverlaySupport.shouldSkipRendering(alpha)) {
			ci.cancel();
			return;
		}

		int drawColor = ActionBarOverlaySupport.buildDrawColor(this.overlayMessageTime, this.animateOverlayMessageColor, alpha);
		List<FormattedCharSequence> lines = this.betterActionBar$splitOverlayLines();
		this.betterActionBar$drawCenteredLines(guiGraphics, lines, drawColor);

		ci.cancel();
	}

	private boolean betterActionBar$shouldHandleMultilineOverlay() {
		return ActionBarOverlaySupport.shouldHandleMultilineOverlay(this.overlayMessageString, this.overlayMessageTime);
	}

	private List<FormattedCharSequence> betterActionBar$splitOverlayLines() {
		Component normalizedMessage = Component.literal(ActionBarOverlaySupport.normalizeOverlayText(this.overlayMessageString));
		return this.minecraft.font.split(normalizedMessage, ActionBarOverlaySupport.splitWidthUnlimited());
	}

	private void betterActionBar$drawCenteredLines(GuiGraphicsExtractor guiGraphics, List<FormattedCharSequence> lines, int drawColor) {
		int lineStep = ActionBarOverlaySupport.lineStep();
		int firstLineY = ActionBarOverlaySupport.firstLineY(guiGraphics.guiHeight(), lines.size());
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			int x = ActionBarOverlaySupport.centeredX(guiGraphics.guiWidth(), this.minecraft.font.width(line));
			int y = firstLineY + i * lineStep;
			guiGraphics.text(this.minecraft.font, line, x, y, drawColor, true);
		}
	}
}
