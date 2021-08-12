package me.duncanruns.autoreset.mixin;

import me.duncanruns.autoreset.AutoReset;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static final Identifier GOLD_BOOTS = new Identifier("textures/item/golden_boots.png");
    private static final Identifier DIAMOND_BOOTS = new Identifier("textures/item/diamond_boots.png");
    private static final Identifier SEEDS = new Identifier("textures/item/wheat_seeds.png");
    private TextFieldWidget seedTextField;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>(Z)V", at = @At("TAIL"))
    private void constructor(boolean doBackgroundFade, CallbackInfo ci) {
//        this.seedTextField.setRenderTextProvider();
    }
    @Inject(method = "init", at = @At("TAIL"))
    private void initMixin(CallbackInfo info) {
        assert this.client != null;

        // If auto reset mode is on, instantly switch to create world menu.
        if (AutoReset.isPlaying) {
            client.openScreen(new CreateWorldScreen(this));
        } else if (!this.client.isDemo()) {


            int y = this.height / 4 + 48;
            // Add new button for starting auto resets.
            this.addButton(new ButtonWidget(this.width / 2 - 124, y, 20, 20, new LiteralText(""), (buttonWidget) -> {
                AutoReset.isPlaying = true;
                AutoReset.isSetSeed = seedTextField.isVisible() && !seedTextField.getText().isEmpty();
                client.openScreen(new CreateWorldScreen(this));
            }));

            // Add a new text field for the seed
            this.seedTextField = this.addChild(new TextFieldWidget(this.textRenderer, (this.width / 2)-100, y-24, 200, 20, new TranslatableText("selectWorld.enterSeed")));
            AutoReset.log(Level.INFO, String.format("%d, %d", seedTextField.x, seedTextField.y));
            seedTextField.setText(AutoReset.seed);
            seedTextField.setChangedListener((string) -> {
                AutoReset.log(Level.INFO, string);
                AutoReset.seed = seedTextField.getText();
                if (string.isEmpty()) {
                    seedTextField.setSuggestion("-3294725893620991126");
                } else {
                    seedTextField.setSuggestion("");
                }
            });

            // Hide by default
            seedTextField.setVisible(false);

            // Add a new button to show/hide the text field
            this.addButton(new ButtonWidget(this.width / 2 - 124, y+24, 20, 20, new LiteralText(""), (buttonWidget) -> {
                seedTextField.setVisible(!seedTextField.visible);
            }));

            if (seedTextField.getText().isEmpty()) {
                seedTextField.setSuggestion("-3294725893620991126");
            }
        }
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"))
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        AutoReset.log(Level.INFO, String.format("%f, %f", mouseX, mouseY));
    }


        @Inject(method = "render", at = @At("TAIL"))
    private void goldBootsOverlayMixin(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int y = this.height / 4 + 48;
        //  {{ Boots button
        if (this.seedTextField.visible) {
            this.client.getTextureManager().bindTexture(DIAMOND_BOOTS);
        } else {
            this.client.getTextureManager().bindTexture(GOLD_BOOTS);
        }
        drawTexture(matrices,(width/2)-122,y+2,0.0F,0.0F,16,16,16,16);
        // }} Boots button

        // Seed button
        this.client.getTextureManager().bindTexture(SEEDS);
        drawTexture(matrices,(width/2)-122,y+2+24,0.0F,0.0F,16,16,16,16);

        // TitleScreen only renders buttons by default, add special case for the TextField.
        this.seedTextField.render(matrices, mouseX, mouseY, delta);
    }
}
