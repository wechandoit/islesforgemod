package net.wechandoit.islesforgemod.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public class MixinContainerScreen extends AbstractGui {

    // on mouse clicks
    @Inject(method = "drawItemStack", at = @At("TAIL"))
    public void drawItemStack(ItemStack stack, int x, int y, String amountText, CallbackInfo ci) {
        MiscUtils.renderAmountOnCrates(stack, x, y, getBlitOffset());
    }

    // default click
    @Inject(method = "moveItems", at = @At("TAIL"))
    public void moveItems(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        MiscUtils.renderAmountOnCrates(slot.getStack(), slot.xPos , slot.yPos, getBlitOffset());
    }

}
