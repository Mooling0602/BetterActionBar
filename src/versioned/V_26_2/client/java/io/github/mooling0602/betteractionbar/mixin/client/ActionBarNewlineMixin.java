package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.client.ActionBarOverlaySupport;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.InGameHud.class)
public abstract class ActionBarNewlineMixin {

    @Shadow
    @Final
    private MinecraftClient minecraft;

    @Shadow
    @Nullable
    private Text overlayMessage;

    @Shadow
    private int overlayMessageTime;

    @Shadow
    private boolean animateOverlayMessageColor;

    @Unique
    @Inject(
        method = "renderOverlayMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void betterActionBar$renderMultilineOverlay(
        DrawContext guiGraphics,
        RenderTickCounter deltaTracker,
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
        List<OrderedText> lines = this.betterActionBar$splitOverlayLines();
        this.betterActionBar$drawCenteredLines(guiGraphics, lines, drawColor);

        ci.cancel();
    }

    @Unique
    private boolean betterActionBar$shouldHandleMultilineOverlay() {
        return ActionBarOverlaySupport.shouldHandleMultilineOverlay(
            this.overlayMessage == null
                ? null
                : this.overlayMessage.getString(),
            this.overlayMessageTime
        );
    }

    @Unique
    private List<OrderedText> betterActionBar$splitOverlayLines() {
        String rawText = this.overlayMessage.getString();
        String normalizedText = ActionBarOverlaySupport.normalizeNewLineBreaks(
            rawText
        );
        if (normalizedText.equals(rawText)) {
            return this.minecraft.textRenderer.wrapLines(
                this.overlayMessage,
                ActionBarOverlaySupport.splitWidthUnlimited()
            );
        }
        return this.minecraft.textRenderer.wrapLines(
            Text.literal(normalizedText).setStyle(
                this.overlayMessage.getStyle()
            ),
            ActionBarOverlaySupport.splitWidthUnlimited()
        );
    }

    @Unique
    private void betterActionBar$drawCenteredLines(
        DrawContext guiGraphics,
        List<OrderedText> lines,
        int drawColor
    ) {
        int lineStep = ActionBarOverlaySupport.lineStep();
        int firstLineY = ActionBarOverlaySupport.firstLineY(
            guiGraphics.getScaledWindowHeight(),
            lines.size()
        );
        for (int i = 0; i < lines.size(); i++) {
            OrderedText line = lines.get(i);
            int x = ActionBarOverlaySupport.centeredX(
                guiGraphics.getScaledWindowWidth(),
                this.minecraft.textRenderer.getWidth(line)
            );
            int y = firstLineY + i * lineStep;
            guiGraphics.drawText(
                this.minecraft.textRenderer,
                line,
                x,
                y,
                drawColor,
                true
            );
        }
    }
}
