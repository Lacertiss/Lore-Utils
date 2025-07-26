package de.lacertis.loreutils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class MessageManager {
    private static final String PREFIX = "&8[&bLoreUtils&8] &r";

    public static void sendChatColored(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MutableText text = parseColorCodes(PREFIX + message);
            MinecraftClient.getInstance().player.sendMessage(text, false); // Chat
        }
    }

    public static void sendActionBarColored(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MutableText text = parseColorCodes(PREFIX + message);
            MinecraftClient.getInstance().player.sendMessage(text, true); // ActionBar
        }
    }

    public static void sendTitleColored(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            MutableText text = parseColorCodes(PREFIX + message);
            InGameHud hud = client.inGameHud;
            hud.setTitle(text);
        }
    }

    private static MutableText parseColorCodes(String message) {
        MutableText result = Text.literal("");
        Formatting current = Formatting.RESET;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '&' && i + 1 < message.length()) {
                Formatting f = getFormatting(message.charAt(i + 1));
                if (f != null) {
                    current = f;
                    i++;
                    continue;
                }
            }
            result.append(Text.literal(String.valueOf(c)).formatted(current));
        }
        return result;
    }

    private static Formatting getFormatting(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Formatting.BLACK;
            case '1' -> Formatting.DARK_BLUE;
            case '2' -> Formatting.DARK_GREEN;
            case '3' -> Formatting.DARK_AQUA;
            case '4' -> Formatting.DARK_RED;
            case '5' -> Formatting.DARK_PURPLE;
            case '6' -> Formatting.GOLD;
            case '7' -> Formatting.GRAY;
            case '8' -> Formatting.DARK_GRAY;
            case '9' -> Formatting.BLUE;
            case 'a' -> Formatting.GREEN;
            case 'b' -> Formatting.AQUA;
            case 'c' -> Formatting.RED;
            case 'd' -> Formatting.LIGHT_PURPLE;
            case 'e' -> Formatting.YELLOW;
            case 'f' -> Formatting.WHITE;
            case 'k' -> Formatting.OBFUSCATED;
            case 'l' -> Formatting.BOLD;
            case 'm' -> Formatting.STRIKETHROUGH;
            case 'n' -> Formatting.UNDERLINE;
            case 'o' -> Formatting.ITALIC;
            case 'r' -> Formatting.RESET;
            default  -> null;
        };
    }
}