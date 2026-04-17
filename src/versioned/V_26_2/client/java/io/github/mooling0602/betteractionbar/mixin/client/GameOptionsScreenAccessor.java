package io.github.mooling0602.betteractionbar.mixin.client;

import net.minecraft.client.gui.widget.OptionListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.screen.option.GameOptionsScreen.class)
public interface GameOptionsScreenAccessor {
    @Accessor("body")
    OptionListWidget betterActionBar$getBody();
}
