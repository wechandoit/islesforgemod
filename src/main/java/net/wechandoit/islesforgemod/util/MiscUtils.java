package net.wechandoit.islesforgemod.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.wechandoit.islesforgemod.Islesforgemod;
import net.wechandoit.islesforgemod.config.IslesAddonConfig;

import java.util.*;

public class MiscUtils {

    private static String cratesSignature = "htUMuy0MbM1YSwEL/xTBC3nSZHY9GtPwiwicjZLjV9aifGMj3t0PUwmhVqwbMXDdp0Yzi4Roepa97cK8lELyjVg/iT2zKzlvISqQYtEcNEan+XBTfFM/H1MUZTV/+N4Hcxf0FEuNT9wJm3Sa+3pZbg2VFTPAqBbQDsTvnFDHHDGz4mR4R5D7w0gbI8i28pIPDp5eGB93RDX8FYEDoEwIiBfqgmT2KASec6pq//xonIw96LTXM7Hg+EZauHhZbvXN+eessLqprYqLRlVEIdzj/tobw+VcuklcjdlQOp8OOp08QmTa2Hz7sseg9NavBAWFz0O0bRnaPZ7EAZ1yFQzXiQgXKK8/sf6IJWoH8hsBdDzg6IDFMrWhHX1qFHAySxSYXC2cFyyNLjy5KGz6DsCaoTpPmwgSOVZqzDePXBiePhQPRN4M3XD6qT8fRJoImWaIvEDgy8MiYWwhC2OidRbXwsj/L6RO6CVilN7N9TdhERqsx0QqMNP/tT52CLpS13KB81A7Zmh+YZ0fgh37CgiU3IbVLbsgICh3J8D8RDgqyvhmngt01wW+yg0W2RF7uxeXUE8LMjvWpYKi0r53uQj9s9K9MRGFv+3oEMwlU2Rl/WT/8+yVfwUihWJ8QNNki8KprXBy4eUz1oG5UM1pFA6KpBTVgds0ZyBIGUI7uvfoNG4=";
    private static final int GUI_OVERLAY_WIDTH_THRESH = 16;
    private static FontRenderer fontRenderer = Islesforgemod.client.fontRenderer;

    public static boolean isWordFromListInString(String message, Collection<String> checkList) {
        return getWordFromListInString(message, checkList) != null;
    }

    public static String getWordFromListInString(String message, Collection<String> checkList) {
        for (String string : checkList) {
            if (message.toUpperCase().contains(string.toUpperCase()))
                return string;
        }
        return null;
    }

    public static Stack<String> getStackFromItemResourceString(String message) {
        Stack<String> stack = new Stack<>();
        for (String m : message.split(" ")) {
            m = m.replace(",", "").replace("🪓", "").replace("☘", "");
            if (stack.empty())
                stack.push(m);
            else {
                String peek = stack.peek();
                if (peek.matches(".*\\d.*") || m.matches(".*\\d.*")) {
                    String peekStack = stack.peek();
                    if (m.matches(".*\\d.*") && peekStack.substring(peekStack.length() - 3).contains("and")) {
                        String n = String.valueOf(stack.pop());
                        n = n.replace(" and", "");
                        stack.push(n);
                    }
                    stack.push(m);
                } else {
                    stack.push(stack.pop() + " " + m);
                }
            }

        }
        return stack;
    }

    public static List<String> getAmountListFromAmountMap(HashMap<String, Integer> amountMap) {
        List<String> amountList = new ArrayList<>();
        for (String item : amountMap.keySet()) {
            amountList.add(amountMap.get(item) + " " + item);
        }
        return amountList;
    }

    public static boolean onIsles() {
        try {
            return Islesforgemod.client != null && !Islesforgemod.client.isSingleplayer() && Islesforgemod.client.getCurrentServerData().serverIP != null && Islesforgemod.client.getCurrentServerData().serverIP.contains("skyblockisles.com");
        } catch (NullPointerException exception) {
            return false;
        }
    }


    public static List<String> getScoreboard() {
        try {
            Scoreboard board = Minecraft.getInstance().player.getWorldScoreboard();
            ScoreObjective objective = board.getObjectiveInDisplaySlot(1);
            List<String> lines = new ArrayList<>();
            for (Score score : board.getSortedScores(objective)) {
                ScorePlayerTeam team = board.getPlayersTeam(score.getPlayerName());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (line.trim().length() > 0) {
                        String formatted = TextFormatting.getTextWithoutFormattingCodes(line);
                        lines.add(formatted);
                    }
                }
            }

            if (objective != null) {
                lines.add(objective.getDisplayName().getString());
                Collections.reverse(lines);
            }
            return lines;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static int getAmountInCrate(String string) {

        if (string == null || string.trim().equals("")) return 0;

        String[] temp = string.replaceAll("[\\[\\]{}:,']", "")
                .replace("text", "")
                .replace("color", "")
                .replace("italic", "")
                .replace("false", "")
                .split("\"");

        for (String line : temp) {
            if (line.matches(".*\\d.*") && line.substring(0, 1).matches(".*\\d.*")) {
                return Integer.parseInt(line.split(" ")[0]);
            }
        }
        return 0;
    }

    public static boolean isCrate(ItemStack stack) {
        return (stack != null && stack.getDisplayName() != null && stack.getDisplayName().getString().contains("Crated") && stack.getTag().get("SkullOwner") != null && stack.getTag().get("SkullOwner").toString().contains(cratesSignature));
    }

    public static boolean isRing(ItemStack stack) {
        return (stack != null && stack.getDisplayName() != null && stack.getDisplayName().getString().contains("Ring of"));
    }

    public static void renderAmountText(MatrixStack matrices, ItemStack stack, int x, int y, int z, int amount) {

        ITextComponent message = ITextComponent.getTextComponentOrEmpty(String.valueOf(amount));

        RenderSystem.enableBlend();
        float scaleRatio = 16 / 20f;
        if (amount < 100) scaleRatio = 1;
        float messageWidth =
                Minecraft.getInstance().fontRenderer.getStringWidth(message.getString()) / scaleRatio;
        float fontHeight = Minecraft.getInstance().fontRenderer.FONT_HEIGHT / scaleRatio;
        matrices.push();
        if (messageWidth * scaleRatio > 20)
            scaleRatio = GUI_OVERLAY_WIDTH_THRESH / messageWidth;

        matrices.scale(scaleRatio, scaleRatio, 1f);
        matrices.translate(0, 0, z + 500);
        if (amount < 100)
            AbstractGui.drawCenteredString(matrices, fontRenderer, message,
                    x + 8, y + 8,
                    TextFormatting.YELLOW.getColor());
        else
            AbstractGui.drawCenteredString(matrices, fontRenderer, message,
                    (int) ((x + 8) / scaleRatio), (int) ((y + GUI_OVERLAY_WIDTH_THRESH - (fontHeight / 2)) / scaleRatio),
                    TextFormatting.YELLOW.getColor());

        matrices.pop();
        RenderSystem.disableBlend();

    }

    public static void renderAmountOnCrates(ItemStack stack, int x, int y, int z) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.getCompound("display") != null) {
            CompoundNBT nbtDisplay = nbt.getCompound("display");
            if(nbtDisplay != null && nbtDisplay.get("Lore") != null) {
                String rawLore = nbtDisplay.get("Lore").toString();
                if (IslesAddonConfig.CONFIG.get("crate-icon-amount", Boolean.class)) {
                    if (isCrate(stack)) {
                        int amount = getAmountInCrate(rawLore);
                        renderAmountText(new MatrixStack(), stack, x, y, z, amount);
                    }
                } else if (IslesAddonConfig.CONFIG.get("ring-icon-amount", Boolean.class)) {
                    if (isRing(stack)) {
                        int amount = 0;
                        renderAmountText(new MatrixStack(), stack, x, y, z, amount);
                    }
                }
            }
        }
    }

    public static ITextComponent getMessage(String message, char color) {
        return new StringTextComponent(message).mergeStyle(TextFormatting.fromFormattingCode(color));
    }
}
