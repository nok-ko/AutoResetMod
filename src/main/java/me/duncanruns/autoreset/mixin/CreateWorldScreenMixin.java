package me.duncanruns.autoreset.mixin;

import me.duncanruns.autoreset.AutoReset;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Pair;
import net.minecraft.world.Difficulty;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Shadow
    private Difficulty field_24289;
    @Shadow
    private Difficulty field_24290;
    @Shadow
    private TextFieldWidget levelNameField;

    @Shadow
    protected abstract void createLevel();

    @Shadow @Final public MoreOptionsDialog moreOptionsDialog;

    @Inject(method = "init", at = @At("TAIL"))
    private void autoStartMixin(CallbackInfo info) {
        // If auto reset mode is on, set difficulty to easy and instantly create world.
        if (AutoReset.isPlaying) {
            field_24289 = Difficulty.EASY;
            field_24290 = Difficulty.EASY;

            // Worldgen settings, where we set the seed
            if (AutoReset.isSetSeed) {
                Properties worldGenProperties = new Properties();
                worldGenProperties.setProperty("level-seed", AutoReset.seed);
                ((OptionsAccessor) moreOptionsDialog).setGeneratorOptions(GeneratorOptions.fromProperties(worldGenProperties));
            }

            Pair<Integer, String> attempt = AutoReset.getNextAttempt(moreOptionsDialog.getGeneratorOptions(false).getSeed());
            levelNameField.setText(String.format("Speedrun #%d (%s)", attempt.getLeft(), attempt.getRight()));
            createLevel();
        }
    }
}
