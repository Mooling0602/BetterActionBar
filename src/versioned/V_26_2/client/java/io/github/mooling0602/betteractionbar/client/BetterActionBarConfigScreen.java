package io.github.mooling0602.betteractionbar.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public final class BetterActionBarConfigScreen extends Screen {
	private final Screen lastScreen;
	private final BetterActionBarConfig workingCopy;
	private final List<TextFieldWidget> newlineInputs = new ArrayList<>();
	private TextFieldWidget lineSpacingField;

	public BetterActionBarConfigScreen(Screen lastScreen) {
		super(Text.translatable("screen.betteractionbar.config.title"));
		this.lastScreen = lastScreen;
		this.workingCopy = BetterActionBarConfigManager.copyOf();
	}

	@Override
	protected void init() {
		this.newlineInputs.clear();
		int contentWidth = Math.min(300, this.width - 40);
		int x = (this.width - contentWidth) / 2;
		int y = 30;

		this.addDrawableChild(new TextWidget(x, y, contentWidth, 20, Text.translatable("screen.betteractionbar.config.line_spacing"), this.textRenderer));
		this.lineSpacingField = new TextFieldWidget(this.textRenderer, x, y + 18, contentWidth, 20, Text.empty());
		this.lineSpacingField.setText(Float.toString(this.workingCopy.lineSpacingPx()));
		this.addDrawableChild(this.lineSpacingField);

		y += 52;
		this.addDrawableChild(new TextWidget(x, y, contentWidth, 20, Text.translatable("screen.betteractionbar.config.newline_break"), this.textRenderer));
		y += 18;
		for (String marker : this.workingCopy.newlineBreak()) {
			this.newlineInputs.add(this.addMarkerField(x, y, contentWidth, marker));
			y += 24;
		}
		this.newlineInputs.add(this.addMarkerField(x, y, contentWidth, ""));

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.betteractionbar.config.add"), button -> this.addMarkerRow(contentWidth))
			.dimensions(x, this.height - 50, 98, 20)
			.build());
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.betteractionbar.config.save"), button -> this.saveAndClose())
			.dimensions(x + contentWidth - 98, this.height - 50, 98, 20)
			.build());
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.client.setScreenAndRender(this.lastScreen))
			.dimensions(x + contentWidth - 200, this.height - 50, 98, 20)
			.build());
	}

	private TextFieldWidget addMarkerField(int x, int y, int width, String value) {
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, 20, Text.empty());
		field.setText(value);
		this.addDrawableChild(field);
		return field;
	}

	private void addMarkerRow(int width) {
		int y = 92 + (this.newlineInputs.size() - 1) * 24;
		this.newlineInputs.add(this.addMarkerField((this.width - width) / 2, y, width, ""));
	}

	private void saveAndClose() {
		this.workingCopy.setLineSpacingPx(this.parseLineSpacing());
		this.workingCopy.setNewlineBreak(this.collectMarkers());
		BetterActionBarConfigManager.save(this.workingCopy);
		this.client.setScreenAndRender(this.lastScreen);
	}

	private float parseLineSpacing() {
		try {
			return Float.parseFloat(this.lineSpacingField.getText());
		} catch (RuntimeException ignored) {
			return BetterActionBarConfig.createDefault().lineSpacingPx();
		}
	}

	private List<String> collectMarkers() {
		List<String> markers = new ArrayList<>();
		for (TextFieldWidget input : this.newlineInputs) {
			String value = input.getText();
			if (!value.isEmpty()) {
				markers.add(value);
			}
		}
		return markers;
	}
}
