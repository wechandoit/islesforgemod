package net.wechandoit.islesforgemod.mixin;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.wechandoit.islesforgemod.config.IslesAddonConfig;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NewChatGui.class)
public abstract class MixinNewChatGui {

    @Shadow public abstract void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId);

    @Shadow public abstract void deleteChatLine(int id);

    @Shadow @Final public List<ChatLine<ITextComponent>> chatLines;
    @Shadow @Final public List<ChatLine<IReorderingProcessor>> drawnChatLines;

    private int amount;
    private boolean sentStackMessage = false;
    private ITextComponent lastMessage = null;
    private ITextComponent stackMessage = null;

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("TAIL"),
            cancellable = true)
    private void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId, CallbackInfo ci) {
        if (IslesAddonConfig.CONFIG.get("stack-chat", Boolean.class)) {
            if (!sentStackMessage) {
                ITextComponent stackText = null;
                if (stackMessage != null && chatComponent.getString().equals(stackMessage.getString())) {
                    if (amount == 1) removeLastSimilarMessage(stackMessage);
                    if (stackMessage != null) removeLastSimilarMessage(stackMessage);
                    if (lastMessage != null) removeLastSimilarMessage(lastMessage);
                    amount++;

                    ITextComponent amountString =
                            MiscUtils.getMessage((" (x%amount%)").replace(
                                    "%amount%",
                                    String.valueOf(amount)), 'b');
                    if (chatComponent.getSiblings().isEmpty()) {
                        stackText =
                                chatComponent.copyRaw().setStyle(chatComponent.getStyle()).appendSibling(amountString);
                    } else {
                        chatComponent.getSiblings().add(amountString);
                        stackText = chatComponent;
                    }
                } else {
                    amount = 1;
                    stackMessage = chatComponent;
                    lastMessage = null;
                }
                if (amount > 1 && stackText != null && !ci.isCancelled()) {
                    sentStackMessage = true;
                    printChatMessageWithOptionalDeletion(stackText, 0);
                    lastMessage = stackText;
                }
            } else {
                sentStackMessage = false;
            }
        }
    }

    @Inject(method = "clearChatMessages", at = @At("HEAD"))
    public void onClearMessages(boolean clearHistory, CallbackInfo ci) {
        this.chatLines.clear();
    }

    private void removeLastSimilarMessage(ITextComponent similar) {
        int line = -1;
        for (int i = chatLines.size() - 1; i >= 0; i--) {
            if (chatLines.get(i).getLineString().getString().equals(similar.getString())) {
                line = i;
            }
        }
        if (line >= 0) removeChatLine(line);
    }

    private void removeChatLine(int line) {
        if (chatLines.size() > line) chatLines.remove(line);
        if (drawnChatLines.size() > line) drawnChatLines.remove(line);
    }

}
