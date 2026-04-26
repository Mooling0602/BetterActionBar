package io.github.mooling0602.betteractionbar.mixin.client;

import com.mojang.serialization.JsonOps;
import io.github.mooling0602.betteractionbar.client.ActionBarOverlaySupport;
import io.github.mooling0602.betteractionbar.client.BetterActionBarClient;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ActionBarNewlineMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private Component overlayMessageString;

    @Shadow
    private int overlayMessageTime;

    @Shadow
    private boolean animateOverlayMessageColor;

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void betterActionBar$logOverlayMessage(
        Component component,
        boolean animateColor,
        CallbackInfo ci
    ) {
        if (component != null) {
            BetterActionBarClient.LOG.info(
                "Received actionbar [{}]: {}",
                component.getClass().getSimpleName(),
                ComponentSerialization.CODEC
                    .encodeStart(JsonOps.INSTANCE, component)
                    .getOrThrow()
            );
        }
    }

    @Inject(
        method = "renderOverlayMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void betterActionBar$renderMultilineOverlay(
        GuiGraphics guiGraphics,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
        if (!this.betterActionBar$shouldHandleMultilineOverlay()) {
            return;
        }

        int alpha = ActionBarOverlaySupport.calculateOverlayAlpha(
            this.overlayMessageTime
        );
        if (ActionBarOverlaySupport.shouldSkipRendering(alpha)) {
            ci.cancel();
            return;
        }

        int drawColor = ActionBarOverlaySupport.buildDrawColor(
            this.overlayMessageTime,
            this.animateOverlayMessageColor,
            alpha
        );
        List<FormattedCharSequence> lines =
            this.betterActionBar$splitOverlayLines();
        this.betterActionBar$drawCenteredLines(guiGraphics, lines, drawColor);

        ci.cancel();
    }

    @Unique
    private boolean betterActionBar$shouldHandleMultilineOverlay() {
        return ActionBarOverlaySupport.shouldHandleMultilineOverlay(
            this.overlayMessageString == null
                ? null
                : this.overlayMessageString.getString(),
            this.overlayMessageTime
        );
    }

    @Unique
    private List<FormattedCharSequence> betterActionBar$splitOverlayLines() {
        String rawText = "";
        if (this.overlayMessageString != null) {
            rawText = this.overlayMessageString.getString();
        }
        String normalizedText = ActionBarOverlaySupport.normalizeNewLineBreaks(
            rawText
        );
        if (normalizedText.equals(rawText)) {
            return this.minecraft.font.split(
                this.overlayMessageString,
                ActionBarOverlaySupport.splitWidthUnlimited()
            );
        }
        Component transformed =
            ActionBarOverlaySupport.normalizeNewLinesInComponent(
                this.overlayMessageString
            );
        return this.minecraft.font.split(
            transformed,
            ActionBarOverlaySupport.splitWidthUnlimited()
        );
    }

    @Unique
    private void betterActionBar$drawCenteredLines(
        GuiGraphics guiGraphics,
        List<FormattedCharSequence> lines,
        int drawColor
    ) {
        int lineStep = ActionBarOverlaySupport.lineStep();
        int firstLineY = ActionBarOverlaySupport.firstLineY(
            guiGraphics.guiHeight(),
            lines.size()
        );
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int x = ActionBarOverlaySupport.centeredX(
                guiGraphics.guiWidth(),
                this.minecraft.font.width(line)
            );
            int y = firstLineY + i * lineStep;
            guiGraphics.drawString(
                this.minecraft.font,
                line,
                x,
                y,
                drawColor,
                true
            );
        }
    }
}
