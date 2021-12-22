package net.wechandoit.islesforgemod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ITextComponent;
import net.wechandoit.islesforgemod.config.IslesAddonConfig;
import net.wechandoit.islesforgemod.util.EXPUtils;
import net.wechandoit.islesforgemod.util.MiscUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@Mixin(ClientPlayNetHandler.class)
public class MixinClientPlayNetHandler {

    @Shadow private Minecraft client;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void handleChat(SChatPacket packetIn, CallbackInfo ci) {
        if (IslesAddonConfig.CONFIG.get("custom-message", Boolean.class)) {
            ITextComponent message = packetIn.getChatComponent();
            if (MiscUtils.onIsles()) {
                if (message.getString().contains("[ITEM]") && !message.getString().contains("Cornucopia")) {
                    //Minecraft.getInstance().player.sendChatMessage("no");
                    ClientPlayerEntity player = Minecraft.getInstance().player;
                    // check for rare item
                    if (message.getString().contains("Raw") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.fishXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(),
                                EXPUtils.fishXPMap, player);
                    } else if (!message.getString().contains("Hide") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.cookingXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(), EXPUtils.cookingXPMap, player);
                    } else if ((message.getString().contains("Log") || message.getString().contains("Bark")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.foragingXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        if (message.getString().contains("🪓"))
                            sendMessageFromList(message.getString(), EXPUtils.foragingXPMap, Minecraft.getInstance().player, true);
                        else
                            sendMessageFromList(message.getString(), EXPUtils.foragingXPMap, Minecraft.getInstance().player);
                    } else if (message.getString().contains("Handle") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.foragingXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(), EXPUtils.carvingXPMap, Minecraft.getInstance().player);
                    } else if (MiscUtils.isWordFromListInString(message.getString(), EXPUtils.farmingXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(), EXPUtils.farmingXPMap, Minecraft.getInstance().player);
                    } else if ((message.getString().contains("Ore") || message.getString().contains("Chunk") || message.getString().contains("Coal") || message.getString().contains("Ice") || message.getString().contains("Essence") || message.getString().contains("Slab") || message.getString().contains("Cannonball")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.miningXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(), EXPUtils.miningXPMap, Minecraft.getInstance().player);
                    } else if ((message.getString().contains("Molten") || message.getString().contains("Bar")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.smeltingXPMap.keySet().stream().collect(Collectors.toList()))) {
                        ci.cancel();
                        sendMessageFromList(message.getString(), EXPUtils.smeltingXPMap, Minecraft.getInstance().player);
                    }
                }
            }
        }
    }

    private ITextComponent sendMessageFromList(String message, Map<String, Integer> xpmap, ClientPlayerEntity player) {
        return sendMessageFromList(message, xpmap, player, false);
    }

    private ITextComponent sendMessageFromList(String message, Map<String, Integer> xpmap, ClientPlayerEntity player, boolean isLumberBuff) {
        return sendMessageFromList(message, xpmap, player, isLumberBuff, false);
    }

    private ITextComponent sendMessageFromList(String message, Map<String, Integer> xpmap, ClientPlayerEntity player, boolean isLumberBuff, boolean isROLProc) {
        float multiplier = isLumberBuff ? 1.5F : 1;
        Stack stack = MiscUtils.getStackFromItemResourceString(message.substring(7));

        HashMap<String, Integer> itemAmountMap = new HashMap<>();
        int maxAmount = 0;
        while (stack.stream().count() >= 2) {
            String type = MiscUtils.getWordFromListInString(String.valueOf(stack.pop()), xpmap.keySet().stream().collect(Collectors.toList()));
            int amount = Integer.parseInt(String.valueOf(stack.pop()));
            if (amount > maxAmount && xpmap.get(type) > 0) {
                maxAmount = amount;
            }
            if (itemAmountMap.containsKey(type)) {
                itemAmountMap.put(type, itemAmountMap.get(type) + amount);
            } else {
                itemAmountMap.put(type, amount);
            }
        }

        int totalXP = 0;
        boolean hasBark = false, hasLog = false, isCooking = false;
        for (String type : itemAmountMap.keySet()) {
            if (type.contains("Bark")) hasBark = true;
            if (type.contains("Log")) hasLog = true;
            if (EXPUtils.cookingXPMap.keySet().contains(type)) isCooking = true;

            if (!(hasBark && hasLog)) if (!isCooking)
                totalXP += xpmap.get(type) * itemAmountMap.get(type);
            else {
                totalXP += xpmap.get(type) * multiplier;
                maxAmount = 1;
            }
        }

        if (Minecraft.getInstance().player != null) {
            ITextComponent msg = MiscUtils.getMessage("+" + totalXP + " XP (" + String.join(", ", MiscUtils.getAmountListFromAmountMap(itemAmountMap)) + ")", getColorFromAmount(maxAmount));
            if (isLumberBuff)
                msg = msg.copyRaw().appendSibling(MiscUtils.getMessage(" +(x1.5 XP 🪓)", 'd'));
            if (isROLProc)
                msg = msg.copyRaw().appendSibling(MiscUtils.getMessage(" (☘)", 'e'));
            client.player.sendStatusMessage(msg, false);
        }

        return null;
    }

    private char getColorFromAmount(int amount) {
        if (amount == 1) {
            return 'a';
        } else if (amount > 1 && amount <= 20) {
            return '5';
        } else {
            return '6';
        }
    }

}
