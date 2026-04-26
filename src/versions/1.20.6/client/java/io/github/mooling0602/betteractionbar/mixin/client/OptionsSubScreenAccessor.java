package io.github.mooling0602.betteractionbar.mixin.client;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenAccessor {
    @Accessor("options")
    Options getOptions();
}
