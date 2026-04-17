package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.BetterActionBarMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsButtonMixin {

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void betterActionBar$appendButton(CallbackInfo ci) {
        try {
            BetterActionBarMod.LOGGER.debug(
                "Injecting BetterActionBar button into AccessibilityOptionsScreen"
            );

            // Safely cast to GameOptionsScreenAccessor
            if (
                !(this instanceof
                        io.github.mooling0602.betteractionbar.mixin.client.GameOptionsScreenAccessor)
            ) {
                BetterActionBarMod.LOGGER.warn(
                    "Failed to cast to GameOptionsScreenAccessor"
                );
                return;
            }

            OptionListWidget list = (
                (io.github.mooling0602.betteractionbar.mixin.client.GameOptionsScreenAccessor) this
            ).betterActionBar$getBody();

            if (list == null) {
                BetterActionBarMod.LOGGER.warn(
                    "OptionListWidget 'body' is null"
                );
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                BetterActionBarMod.LOGGER.warn(
                    "MinecraftClient instance is null"
                );
                return;
            }

            // Create the BetterActionBar configuration button
            ButtonWidget button = ButtonWidget.builder(
                Text.translatable("betteractionbar.options.open"),
                b -> {
                    try {
                        if (
                            !(client instanceof
                                    io.github.mooling0602.betteractionbar.mixin.client.MinecraftClientAccessor)
                        ) {
                            BetterActionBarMod.LOGGER.warn(
                                "Failed to cast MinecraftClient to MinecraftClientAccessor"
                            );
                            return;
                        }

                        BetterActionBarMod.LOGGER.debug(
                            "Opening BetterActionBar configuration screen"
                        );
                        (
                            (io.github.mooling0602.betteractionbar.mixin.client.MinecraftClientAccessor) client
                        ).betterActionBar$setScreen(
                            new TemporaryConfigScreen((Screen) (Object) this)
                        );
                    } catch (Exception e) {
                        BetterActionBarMod.LOGGER.error(
                            "Error opening configuration screen",
                            e
                        );
                    }
                }
            )
                .width(150)
                .build();

            // Create a dummy button for the right column (zero width, does nothing)
            ButtonWidget dummy = ButtonWidget.builder(Text.empty(), btn -> {})
                .width(0)
                .build();

            list.addWidgetEntry(button, dummy);
            BetterActionBarMod.LOGGER.debug(
                "Successfully added BetterActionBar button to AccessibilityOptionsScreen"
            );
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error(
                "Error injecting BetterActionBar button",
                e
            );
        }
    }

    // Temporary screen class until BetterActionBarConfigUI is implemented
    private static class TemporaryConfigScreen extends Screen {

        private final Screen parent;

        protected TemporaryConfigScreen(Screen parent) {
            super(Text.translatable("betteractionbar.options.title"));
            this.parent = parent;
            BetterActionBarMod.LOGGER.debug("Created TemporaryConfigScreen");
        }

        @Override
        protected void init() {
            super.init();

            try {
                // Add a back button
                this.addDrawableChild(
                    ButtonWidget.builder(
                        Text.translatable("gui.back"),
                        button -> {
                            try {
                                if (
                                    !(this.client instanceof
                                            io.github.mooling0602.betteractionbar.mixin.client.MinecraftClientAccessor)
                                ) {
                                    BetterActionBarMod.LOGGER.warn(
                                        "Failed to cast MinecraftClient to MinecraftClientAccessor in back button"
                                    );
                                    return;
                                }

                                BetterActionBarMod.LOGGER.debug(
                                    "Returning to parent screen"
                                );
                                (
                                    (io.github.mooling0602.betteractionbar.mixin.client.MinecraftClientAccessor) this.client
                                ).betterActionBar$setScreen(this.parent);
                            } catch (Exception e) {
                                BetterActionBarMod.LOGGER.error(
                                    "Error returning to parent screen",
                                    e
                                );
                            }
                        }
                    )
                        .dimensions(
                            this.width / 2 - 100,
                            this.height - 40,
                            200,
                            20
                        )
                        .build()
                );
            } catch (Exception e) {
                BetterActionBarMod.LOGGER.error(
                    "Error initializing TemporaryConfigScreen",
                    e
                );
            }
        }

        @Override
        public void render(
            net.minecraft.client.gui.DrawContext context,
            int mouseX,
            int mouseY,
            float delta
        ) {
            try {
                this.renderBackground(context, mouseX, mouseY, delta);
                super.render(context, mouseX, mouseY, delta);

                // Draw title
                context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    this.getTitle(),
                    this.width / 2,
                    20,
                    0xFFFFFF
                );

                // Draw placeholder text
                context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable("betteractionbar.options.placeholder"),
                    this.width / 2,
                    this.height / 2 - 10,
                    0xFFFFFF
                );
            } catch (Exception e) {
                BetterActionBarMod.LOGGER.error(
                    "Error rendering TemporaryConfigScreen",
                    e
                );
            }
        }
    }
}
