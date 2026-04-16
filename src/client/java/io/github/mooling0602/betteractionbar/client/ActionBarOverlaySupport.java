package io.github.mooling0602.betteractionbar.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public final class ActionBarOverlaySupport {
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

	private ActionBarOverlaySupport() {
	}

	public static boolean shouldHandleMultilineOverlay(@Nullable Object overlayMessageString, int overlayMessageTime) {
		if (overlayMessageString == null || overlayMessageTime <= 0) {
			return false;
		}

		return normalizeOverlayText(overlayMessageString).contains(NEWLINE);
	}

	public static String normalizeOverlayText(@Nullable Object overlayMessageString) {
		String text = overlayMessageString == null ? "" : extractPlainText(overlayMessageString);
		List<String> breakTokens = new ArrayList<>(BetterActionBarConfigManager.getConfig().newlineBreak());
		breakTokens.removeIf(token -> token == null || token.isEmpty());
		breakTokens.sort(Comparator.comparingInt(String::length).reversed());
		for (String breakToken : breakTokens) {
			text = text.replace(breakToken, NEWLINE);
		}
		return text;
	}

	private static String extractPlainText(Object overlayMessageString) {
		try {
			Method getString = overlayMessageString.getClass().getMethod("getString");
			Object value = getString.invoke(overlayMessageString);
			return value instanceof String ? (String) value : String.valueOf(value);
		} catch (ReflectiveOperationException ignored) {
			return String.valueOf(overlayMessageString);
		}
	}

	public static int calculateOverlayAlpha(int overlayMessageTime) {
		return Math.max(0, Math.min(255, overlayMessageTime * 255 / OVERLAY_FADE_TICKS));
	}

	public static boolean shouldSkipRendering(int alpha) {
		return alpha <= MIN_VISIBLE_ALPHA;
	}

	public static int buildDrawColor(int overlayMessageTime, boolean animateOverlayMessageColor, int alpha) {
		int color = DEFAULT_TEXT_COLOR_RGB;
		if (animateOverlayMessageColor) {
			color = java.awt.Color.HSBtoRGB((float) overlayMessageTime / RAINBOW_HUE_CYCLE_TICKS, RAINBOW_SATURATION, RAINBOW_BRIGHTNESS) & DEFAULT_TEXT_COLOR_RGB;
		}
		return color | (alpha << 24);
	}

	public static int splitWidthUnlimited() {
		return SPLIT_WIDTH_UNLIMITED;
	}

	public static int lineStep() {
		return Math.round(BASE_LINE_HEIGHT * (1.0F + BetterActionBarConfigManager.getConfig().lineSpacingPx()));
	}

	public static int firstLineY(int guiHeight, int lineCount) {
		return guiHeight - BASELINE_OFFSET_FROM_BOTTOM - (lineCount - 1) * lineStep();
	}

	public static int centeredX(int guiWidth, int lineWidth) {
		return (guiWidth - lineWidth) / 2;
	}
}
