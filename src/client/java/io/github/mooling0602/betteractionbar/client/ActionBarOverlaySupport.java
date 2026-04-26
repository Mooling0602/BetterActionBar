package io.github.mooling0602.betteractionbar.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public static Component normalizeNewLinesInComponent(Component component) {
        List<String> tokens = getValidNewLineTokens();
        if (tokens.isEmpty()) {
            return component;
        }
        return normalizeInTree(component, tokens);
    }

    private static Component normalizeInTree(
        Component component,
        List<String> tokens
    ) {
        List<Component> siblings = component.getSiblings();
        List<Component> processedSiblings = new ArrayList<>(siblings.size());
        boolean siblingsChanged = false;
        for (Component sibling : siblings) {
            Component processed = normalizeInTree(sibling, tokens);
            processedSiblings.add(processed);
            if (processed != sibling) {
                siblingsChanged = true;
            }
        }

        String replacedText = null;
        String text = component
            .getContents()
            .visit((textLambda) -> Optional.of(textLambda))
            .orElse(null);
        if (text != null) {
            String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
            for (String token : tokens) {
                normalized = normalized.replace(token, "\n");
            }
            if (!normalized.equals(text)) {
                replacedText = normalized;
            }
        }

        if (replacedText == null && !siblingsChanged) {
            return component;
        }

        MutableComponent result;
        if (replacedText != null) {
            result = Component.literal(replacedText).setStyle(
                component.getStyle()
            );
        } else {
            result = component.copy();
            result.getSiblings().clear();
        }

        for (Component s : processedSiblings) {
            result.append(s);
        }
        return result;
    }

    private static List<String> getValidNewLineTokens() {
        List<String> tokens = BetterActionBarConfig.get().newLineBreaks();
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> valid = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                valid.add(token);
            }
        }
        return valid;
    }
}
