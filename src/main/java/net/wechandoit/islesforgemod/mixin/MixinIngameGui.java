package net.wechandoit.islesforgemod.mixin;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IngameGui.class)
public class MixinIngameGui extends AbstractGui {

    @Inject(method = "renderHotbarItem", at = @At(value = "TAIL"))
    public void renderHotbarItem(int x, int y, float partialTicks, PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        MiscUtils.renderAmountOnCrates(stack, x, y, getBlitOffset());
    }

}
