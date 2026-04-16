package io.github.mooling0602.betteractionbar.client;

import org.jetbrains.annotations.Nullable;

public final class ActionBarOverlaySupport {

    private static final int OVERLAY_FADE_TICKS = 20;
    private static final int MIN_VISIBLE_ALPHA = 8;
    private static final int DEFAULT_TEXT_COLOR_RGB = 0xFFFFFF;
    private static final float RAINBOW_HUE_CYCLE_TICKS = 50.0F;
    private static final float RAINBOW_SATURATION = 0.7F;
    private static final float RAINBOW_BRIGHTNESS = 0.6F;
    private static final int SPLIT_WIDTH_UNLIMITED = 1_000_000;
    private static final int BASE_LINE_HEIGHT = 9;
    private static final int BASELINE_OFFSET_FROM_BOTTOM = 68;

    private ActionBarOverlaySupport() {}

    public static boolean shouldHandleMultilineOverlay(
        @Nullable String overlayMessageString,
        int overlayMessageTime
    ) {
        if (overlayMessageString == null || overlayMessageTime <= 0) {
            return false;
        }

        return normalizeNewLineBreaks(overlayMessageString).contains("\n");
    }

    public static String normalizeNewLineBreaks(String text) {
        return BetterActionBarConfig.normalizeNewLineBreaks(text);
    }

    public static int calculateOverlayAlpha(int overlayMessageTime) {
        return Math.max(
            0,
            Math.min(255, (overlayMessageTime * 255) / OVERLAY_FADE_TICKS)
        );
    }

    public static boolean shouldSkipRendering(int alpha) {
        return alpha <= MIN_VISIBLE_ALPHA;
    }

    public static int buildDrawColor(
        int overlayMessageTime,
        boolean animateOverlayMessageColor,
        int alpha
    ) {
        int color = DEFAULT_TEXT_COLOR_RGB;
        if (animateOverlayMessageColor) {
            color =
                java.awt.Color.HSBtoRGB(
                    (float) overlayMessageTime / RAINBOW_HUE_CYCLE_TICKS,
                    RAINBOW_SATURATION,
                    RAINBOW_BRIGHTNESS
                ) &
                DEFAULT_TEXT_COLOR_RGB;
        }
        return color | (alpha << 24);
    }

    public static int splitWidthUnlimited() {
        return SPLIT_WIDTH_UNLIMITED;
    }

    public static int lineStep() {
        float lineSpacingMultiplier = Math.max(
            0.0F,
            BetterActionBarConfig.get().lineSpacingMultiplier()
        );
        return Math.round(BASE_LINE_HEIGHT * (1 + lineSpacingMultiplier));
    }

    public static int firstLineY(int guiHeight, int lineCount) {
        return (
            guiHeight -
            BASELINE_OFFSET_FROM_BOTTOM -
            (lineCount - 1) * lineStep()
        );
    }

    public static int centeredX(int guiWidth, int lineWidth) {
        return (guiWidth - lineWidth) / 2;
    }
}
