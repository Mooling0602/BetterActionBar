package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.BetterActionBarMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.text.Text;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsButtonMixin {

    @Inject(method = "addOptions", at = @At("HEAD"), cancellable = true)
    private void betterActionBar$appendButton(CallbackInfo ci) {
        try {
            if (!(this instanceof OptionsSubScreenAccessor accessor)) {
                BetterActionBarMod.LOGGER.warn(
                    "Failed to cast to OptionsSubScreenAccessor"
                );
                return;
            }

            ci.cancel(); // 取消原方法执行

            Screen screen = (Screen) (Object) this;
            OptionListWidget list = accessor.betterActionBar$getList();
            GameOptions gameOptions = accessor.betterActionBar$getOptions();

            if (list == null) {
                BetterActionBarMod.LOGGER.warn(
                    "OptionListWidget 'body' is null"
                );
                return;
            }

            if (gameOptions == null) {
                BetterActionBarMod.LOGGER.warn("GameOptions instance is null");
                return;
            }

            Object optionsArray = this.betterActionBar$getAccessibilityOptions(
                screen,
                gameOptions
            );
            int optionCount = optionsArray == null ? 0 : Array.getLength(optionsArray);
            if (optionCount == 0) {
                BetterActionBarMod.LOGGER.warn(
                    "No accessibility options found, skipping custom button injection"
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

            ButtonWidget button = ButtonWidget.builder(
                Text.translatable("betteractionbar.options.open"),
                b -> {
                    try {
                        if (!(client instanceof MinecraftClientAccessor clientAccessor)) {
                            BetterActionBarMod.LOGGER.warn(
                                "Failed to cast MinecraftClient to MinecraftClientAccessor"
                            );
                            return;
                        }

                        BetterActionBarMod.LOGGER.debug(
                            "Opening BetterActionBar configuration screen"
                        );
                        clientAccessor.betterActionBar$setScreen(
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

            Object optionsToAdd = this.betterActionBar$copyOptionsRange(
                optionsArray,
                Math.max(0, optionCount - 1)
            );
            if (!this.betterActionBar$addAllOptions(list, optionsToAdd)) {
                BetterActionBarMod.LOGGER.warn("Failed to add accessibility options");
                return;
            }

            ClickableWidget lastOptionWidget = this.betterActionBar$createOptionWidget(
                Array.get(optionsArray, optionCount - 1),
                gameOptions
            );
            if (lastOptionWidget == null) {
                BetterActionBarMod.LOGGER.warn("Failed to create last option widget");
                return;
            }

            // 尝试使用addSmall方法（如果存在），否则使用addWidgetEntry
            if (!this.betterActionBar$addSmallWidgets(list, lastOptionWidget, button)) {
                // 回退到addWidgetEntry
                list.addWidgetEntry(lastOptionWidget, button);
            }
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error(
                "Error injecting BetterActionBar button",
                e
            );
        }
    }

    private Object betterActionBar$getAccessibilityOptions(
        Screen screen,
        GameOptions gameOptions
    ) {
        try {
            Method method = screen
                .getClass()
                .getDeclaredMethod("options", GameOptions.class);
            method.setAccessible(true);
            return method.invoke(screen, gameOptions);
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error("Failed to get options", e);
            return null;
        }
    }

    private Object betterActionBar$copyOptionsRange(Object optionsArray, int size) {
        Object prefix = Array.newInstance(
            optionsArray.getClass().getComponentType(),
            size
        );
        if (size > 0) {
            System.arraycopy(optionsArray, 0, prefix, 0, size);
        }
        return prefix;
    }

    private boolean betterActionBar$addAllOptions(
        OptionListWidget list,
        Object optionsArray
    ) {
        try {
            Method addAll = this.betterActionBar$findListMethod(
                list,
                "addAll",
                optionsArray.getClass()
            );
            if (addAll != null) {
                addAll.invoke(list, optionsArray);
                return true;
            }

            Method addSmall = this.betterActionBar$findListMethod(
                list,
                "addSmall",
                optionsArray.getClass()
            );
            if (addSmall != null) {
                addSmall.invoke(list, optionsArray);
                return true;
            }

            BetterActionBarMod.LOGGER.error(
                "Failed to add accessibility options: no supported list method found"
            );
            return false;
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error("Failed to add accessibility options", e);
            return false;
        }
    }

    private boolean betterActionBar$addSmallWidgets(
        OptionListWidget list,
        ClickableWidget leftWidget,
        ClickableWidget rightWidget
    ) {
        try {
            // 查找接受两个ClickableWidget参数的addSmall方法
            Method addSmall = null;
            Method[] methods = list.getClass().getMethods();
            for (Method method : methods) {
                if (!method.getName().equals("addSmall")) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && 
                    params[0].isAssignableFrom(ClickableWidget.class) && 
                    params[1].isAssignableFrom(ClickableWidget.class)) {
                    addSmall = method;
                    break;
                }
            }
            
            if (addSmall == null) {
                // 也检查声明的方法
                Method[] declaredMethods = list.getClass().getDeclaredMethods();
                for (Method method : declaredMethods) {
                    if (!method.getName().equals("addSmall")) {
                        continue;
                    }
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 2 && 
                        params[0].isAssignableFrom(ClickableWidget.class) && 
                        params[1].isAssignableFrom(ClickableWidget.class)) {
                        addSmall = method;
                        break;
                    }
                }
            }
            
            if (addSmall != null) {
                addSmall.setAccessible(true);
                addSmall.invoke(list, leftWidget, rightWidget);
                BetterActionBarMod.LOGGER.debug("Successfully used addSmall method with two ClickableWidget parameters");
                return true;
            }
            
            BetterActionBarMod.LOGGER.debug("addSmall method with two ClickableWidget parameters not found");
            return false;
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error("Failed to use addSmall method", e);
            return false;
        }
    }

    private Method betterActionBar$findListMethod(
        OptionListWidget list,
        String methodName,
        Class<?> argumentType
    ) {
        Method[] methods = list.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            BetterActionBarMod.LOGGER.debug("Found method {} with {} parameters", methodName, params.length);
            for (int i = 0; i < params.length; i++) {
                BetterActionBarMod.LOGGER.debug("  Param {}: {}", i, params[i].getName());
            }
            if (params.length == 1 && params[0].isAssignableFrom(argumentType)) {
                method.setAccessible(true);
                return method;
            }
        }

        Method[] declaredMethods = list.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].isAssignableFrom(argumentType)) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    private ClickableWidget betterActionBar$createOptionWidget(
        Object option,
        GameOptions gameOptions
    ) {
        try {
            Method createWidget = option
                .getClass()
                .getMethod("createWidget", GameOptions.class);
            Object widget = createWidget.invoke(option, gameOptions);
            return widget instanceof ClickableWidget clickableWidget
                ? clickableWidget
                : null;
        } catch (NoSuchMethodException ignored) {
            try {
                Method createButton = option
                    .getClass()
                    .getMethod("createButton", GameOptions.class);
                Object widget = createButton.invoke(option, gameOptions);
                return widget instanceof ClickableWidget clickableWidget
                    ? clickableWidget
                    : null;
            } catch (Exception e) {
                BetterActionBarMod.LOGGER.error(
                    "Failed to create option widget",
                    e
                );
                return null;
            }
        } catch (Exception e) {
            BetterActionBarMod.LOGGER.error("Failed to create option widget", e);
            return null;
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
                                if (!(this.client instanceof MinecraftClientAccessor clientAccessor)) {
                                    BetterActionBarMod.LOGGER.warn(
                                        "Failed to cast MinecraftClient to MinecraftClientAccessor in back button"
                                    );
                                    return;
                                }

                                BetterActionBarMod.LOGGER.debug(
                                    "Returning to parent screen"
                                );
                                clientAccessor.betterActionBar$setScreen(this.parent);
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
