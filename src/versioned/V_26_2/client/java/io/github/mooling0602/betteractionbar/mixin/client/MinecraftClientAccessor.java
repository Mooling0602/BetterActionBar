package io.github.mooling0602.betteractionbar.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Invoker("setScreenAndRender")
    void betterActionBar$setScreen(Screen screen);
}
