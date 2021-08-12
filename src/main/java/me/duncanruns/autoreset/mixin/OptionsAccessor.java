package me.duncanruns.autoreset.mixin;

import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MoreOptionsDialog.class)
public interface OptionsAccessor {
    @Accessor
    void setGeneratorOptions(GeneratorOptions generatorOptions);
}
