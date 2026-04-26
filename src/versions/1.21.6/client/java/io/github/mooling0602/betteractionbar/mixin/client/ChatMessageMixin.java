package io.github.mooling0602.betteractionbar.mixin.client;

import io.github.mooling0602.betteractionbar.client.RemoteConfigHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.components.ChatComponent.class)
public abstract class ChatMessageMixin {

    @Inject(method = "addServerSystemMessage", at = @At("HEAD"))
    private void betterActionBar$onServerSystemMessage(
        Component message,
        CallbackInfo ci
    ) {
        checkTree(message);
    }

    private static void checkTree(Component component) {
        Style style = component.getStyle();
        if (style != null) {
            HoverEvent hoverEvent = style.getHoverEvent();
            if (
                hoverEvent != null &&
                hoverEvent.action() == HoverEvent.Action.SHOW_TEXT
            ) {
                if (hoverEvent instanceof HoverEvent.ShowText showText) {
                    RemoteConfigHandler.handleConfigText(
                        showText.value().getString()
                    );
                    return;
                }
            }
        }
        for (Component sibling : component.getSiblings()) {
            checkTree(sibling);
        }
    }
}
