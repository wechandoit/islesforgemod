
package net.wechandoit.islesforgemod.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Shadow
    public float zLevel;

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        MiscUtils.renderAmountOnCrates(stack, x, y, this.zLevel);
    }
}