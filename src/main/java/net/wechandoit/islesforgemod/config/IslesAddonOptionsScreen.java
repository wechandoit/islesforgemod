package net.wechandoit.islesforgemod.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Objects;

public class IslesAddonOptionsScreen extends SettingsScreen {

    private OptionsRowList buttons;

    public IslesAddonOptionsScreen(Screen parent) {
        super(parent, (Minecraft.getInstance()).gameSettings, new TranslationTextComponent("isles-addons.options"));
    }

    @Override
    protected void init() {
        this.buttons = new OptionsRowList(Objects.requireNonNull(this.minecraft), this.width, this.height, 32, this.height - 32, 25);
        this.buttons.addOptions(IslesAddonConfig.getOptions());
        this.children.add(this.buttons);
        addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, __ -> {
            IslesAddonConfig.save();
            (Objects.requireNonNull(this.minecraft)).displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.buttons.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 5, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        List<IReorderingProcessor> tooltip = func_243293_a(this.buttons, mouseX, mouseY);
        if (tooltip != null) renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        IslesAddonConfig.save();
    }
}
