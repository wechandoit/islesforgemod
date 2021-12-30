package net.wechandoit.islesforgemod.mixin;

import net.minecraft.client.Minecraft;
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
import java.util.Optional;
import java.util.Stack;

@Mixin(ClientPlayNetHandler.class)
public class MixinClientPlayNetHandler {

    @Shadow
    private Minecraft client;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void handleChat(SChatPacket packetIn, CallbackInfo ci) {
        if (IslesAddonConfig.CONFIG.get("custom-message", Boolean.class)) {
            ITextComponent message = packetIn.getChatComponent();
            if (MiscUtils.onIsles()) {
                if (message.getString().contains("[ITEM]") && !message.getString().contains("Cornucopia")) {
                    // check for rare item
                    if (message.getString().contains("Raw") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.fishXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.fishXPMap);
                    } else if (!message.getString().contains("Hide") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.cookingXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.cookingXPMap);
                    } else if ((message.getString().contains("Log") || message.getString().contains("Bark") || message.getString().contains("Acorn") || message.getString().contains("Arrow Shaft")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.foragingXPMap.keySet())) {
                        ci.cancel();
                        if (message.getString().contains("🪓"))
                            sendMessageFromList(message, EXPUtils.foragingXPMap, true);
                        else
                            sendMessageFromList(message, EXPUtils.foragingXPMap);
                    } else if (message.getString().contains("Handle") && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.foragingXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.carvingXPMap);
                    } else if (MiscUtils.isWordFromListInString(message.getString(), EXPUtils.farmingXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.farmingXPMap);
                    } else if ((message.getString().contains("Ore") || message.getString().contains("Chunk") || message.getString().contains("Coal") || message.getString().contains("Ice") || message.getString().contains("Essence") || message.getString().contains("Slab") || message.getString().contains("Cannonball")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.miningXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.miningXPMap);
                    } else if ((message.getString().contains("Molten") || message.getString().contains("Bar")) && MiscUtils.isWordFromListInString(message.getString(), EXPUtils.smeltingXPMap.keySet())) {
                        ci.cancel();
                        sendMessageFromList(message, EXPUtils.smeltingXPMap);
                    }
                }
            }
        }
    }

    private ITextComponent sendMessageFromList(ITextComponent message, Map<String, Integer> xpmap) {
        return sendMessageFromList(message, xpmap, false);
    }

    private ITextComponent sendMessageFromList(ITextComponent message, Map<String, Integer> xpmap, boolean isLumberBuff) {
        return sendMessageFromList(message, xpmap, isLumberBuff, false);
    }

    private ITextComponent sendMessageFromList(ITextComponent message, Map<String, Integer> xpmap, boolean isLumberBuff, boolean isROLProc) {
        float multiplier = isLumberBuff ? 1.5F : 1;
        Stack<String> stack = MiscUtils.getStackFromItemResourceString(message.getString().substring(7));

        HashMap<String, Integer> itemAmountMap = new HashMap<>();
        int maxAmount = 0;
        while (stack.size() >= 2) {
            String element = stack.pop();
            String type = Optional.ofNullable(MiscUtils.getWordFromListInString(element, xpmap.keySet())).orElse(element);
            
            try {
                int amount = Integer.parseInt(stack.pop());
                if (amount > maxAmount && xpmap.getOrDefault(type, 0) > 0) {
                    maxAmount = amount;
                }
                if (itemAmountMap.containsKey(type)) {
                    itemAmountMap.put(type, itemAmountMap.get(type) + amount);
                } else {
                    itemAmountMap.put(type, amount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int totalXP = 0;
        boolean hasBark = false, hasLog = false, isCooking = false, isShaft = false;
        for (String type : itemAmountMap.keySet()) {
            if (type.contains("Bark")) hasBark = true;
            if (type.contains("Log")) hasLog = true;
            if (type.contains("Arrow Shaft")) isShaft = true;
            if (EXPUtils.cookingXPMap.containsKey(type)) isCooking = true;

            if (!(hasBark && hasLog) && !isCooking && !isShaft)
                totalXP += xpmap.getOrDefault(type, 0) * itemAmountMap.get(type);
            else {
                totalXP += xpmap.getOrDefault(type, 0) * multiplier;
                maxAmount = 1;
            }
        }

        if (client.player != null) {
            ITextComponent msg = MiscUtils.getMessage("+" + totalXP + " XP (" + String.join(", ", MiscUtils.getAmountListFromAmountMap(itemAmountMap)) + ")", getColorFromAmount(maxAmount, isShaft));
            if (isLumberBuff)
                msg.getSiblings().add(MiscUtils.getMessage(" +(x1.5 XP 🪓)", 'd'));
            if (isROLProc)
                msg.getSiblings().add(MiscUtils.getMessage(" (☘)", 'e'));
            client.player.sendStatusMessage(msg, false);
        }

        return message;
    }

    private char getColorFromAmount(int amount, boolean isShaft) {
        if (isShaft || amount == 1) {
            return 'a';
        } else if (amount > 1 && amount <= 20) {
            return '5';
        } else {
            return '6';
        }
    }

}
