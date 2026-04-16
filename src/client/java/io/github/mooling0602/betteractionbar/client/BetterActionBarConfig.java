package io.github.mooling0602.betteractionbar.client;

import java.util.ArrayList;
import java.util.List;

public final class BetterActionBarConfig {

	private static final float DEFAULT_LINE_SPACING_PX = 0.1F;

	private float lineSpacingPx = DEFAULT_LINE_SPACING_PX;
	private List<String> newlineBreak = new ArrayList<>();

	public float lineSpacingPx() {
		return this.lineSpacingPx;
	}

	public List<String> newlineBreak() {
		return this.newlineBreak;
	}

	public void setLineSpacingPx(float lineSpacingPx) {
		this.lineSpacingPx = lineSpacingPx;
	}

	public void setNewlineBreak(List<String> newlineBreak) {
		this.newlineBreak = newlineBreak;
	}

	public void sanitize() {
		if (this.lineSpacingPx < 0.0F) {
			this.lineSpacingPx = 0.0F;
		}

		if (this.newlineBreak == null) {
			this.newlineBreak = new ArrayList<>();
		}
	}

	public static BetterActionBarConfig createDefault() {
		return new BetterActionBarConfig();
	}

	public BetterActionBarConfig copy() {
		BetterActionBarConfig copy = new BetterActionBarConfig();
		copy.lineSpacingPx = this.lineSpacingPx;
		copy.newlineBreak = new ArrayList<>(this.newlineBreak);
		return copy;
	}
}
