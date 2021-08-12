package me.duncanruns.autoreset.mixin;

import me.duncanruns.autoreset.AutoReset;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.util.Language;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NonnullDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// IDE Tax
@SuppressWarnings("ConstantConditions")
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static final Identifier GOLD_BOOTS = new Identifier("textures/item/golden_boots.png");
    private static final Identifier DIAMOND_BOOTS = new Identifier("textures/item/diamond_boots.png");
    private static final Identifier SEEDS = new Identifier("textures/item/wheat_seeds.png");
    private TextFieldWidget seedTextField;
    private ButtonWidget bootsButton;
    private ButtonWidget seedButton;
    private boolean allowSubmit;

    protected TitleScreenMixin(Text title) {
        super(title);
        throw(new RuntimeException("TitleScreenMixin constructor executed?!"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initMixin(CallbackInfo info) {
        // If auto reset mode is on, instantly switch to create world menu.
        if (AutoReset.isPlaying) {
            client.openScreen(new CreateWorldScreen(this));
        } else if (!this.client.isDemo()) {


            int y = this.height / 4 + 48;
            // Add new button for starting auto resets.
            // Get from translation key or English default.
            final Text bootsTextRSG = Language.getInstance().hasTranslation("menu.start_rsg")
                    ? new TranslatableText("menu.start_rsg")
                    : new LiteralText("Start RSG Run!");
            final Text bootsTextSSG = Language.getInstance().hasTranslation("menu.start_ssg")
                    ? new TranslatableText("menu.start_ssg")
                    : new LiteralText("Start SSG Run!");

            bootsButton = addButton(new ButtonWidget(this.width / 2 - 124, y, 20, 20, new LiteralText(""), (buttonWidget) -> {
                if (allowSubmit || !seedTextField.visible) {
                    AutoReset.isPlaying = true;
                    AutoReset.isSetSeed = isSSGRun();
                    client.openScreen(new CreateWorldScreen(this));
                }
            }, (button, matrices, mouseX, mouseY) -> {
                if (isSSGRun()) {
                    renderTooltip(matrices, bootsTextSSG, mouseX, mouseY);
                } else {
                    renderTooltip(matrices, bootsTextRSG, mouseX, mouseY);
                }
            }));

            // Add a new text field for the seed
            seedTextField = addChild(new TextFieldWidget(this.textRenderer, (this.width / 2)-100, y-24, 200, 20, new TranslatableText("selectWorld.enterSeed")));
            seedTextField.setText(AutoReset.seed);

            final String suggestion = "-3294725893620991126";

            seedTextField.setChangedListener((string) -> {
                AutoReset.seed = seedTextField.getText();
                if (string.isEmpty()) {
                    seedTextField.setSuggestion(suggestion);
                } else {
                    seedTextField.setSuggestion("");
//                    AutoReset.log(Level.INFO, string);
                    if (string.contains(";")) {
                        seedTextField.setEditableColor(0xFF0000);
                        allowSubmit = false;
                    } else {
                        seedTextField.setEditableColor(0xE0E0E0);
                        allowSubmit = true;
                    }
                }
            });

            // Hide by default
            seedTextField.setVisible(false);

            // Add a new button to show/hide the text field
            seedButton = addButton(new ButtonWidget(this.width / 2 - 124, y+24, 20, 20, new LiteralText(""), (buttonWidget) -> {
                seedTextField.setVisible(!seedTextField.visible);
            }, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, new TranslatableText("selectWorld.enterSeed"), mouseX, mouseY)));

            if (seedTextField.getText().isEmpty()) {
                seedTextField.setSuggestion(suggestion);
            }
        }
    }

    private boolean isSSGRun() {
        return seedTextField.isVisible() && !seedTextField.getText().isEmpty();
    }

    @Inject(method = "render", at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "net/minecraft/client/gui/screen/TitleScreen.splashText:Ljava/lang/String;"))
    private void renderSeedTextField(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        /*
         TitleScreen only renders buttons by default, add special case for the TextField.
         Render before splash text field access to make the splash text draw on top of the text field.
         It was bugging me, okay?
        */
        this.seedTextField.render(matrices, mouseX, mouseY, delta);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderButtonOverlays(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Boots button
        this.client.getTextureManager().bindTexture(isSSGRun() ? DIAMOND_BOOTS : GOLD_BOOTS);
        drawTexture(matrices, bootsButton.x+2, bootsButton.y+2, 0.0F,0.0F,16,16,16,16);

        // Seed button
        this.client.getTextureManager().bindTexture(SEEDS);
        drawTexture(matrices,seedButton.x+2,seedButton.y+2,0.0F,0.0F,16,16,16,16);
    }
}
