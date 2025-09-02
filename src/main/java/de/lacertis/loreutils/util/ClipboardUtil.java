package de.lacertis.loreutils.util;

import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public final class ClipboardUtil {
    private ClipboardUtil() {}

    public static void setClipboard(String text) {
        String s = text == null ? "" : text;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.keyboard != null) {
                mc.keyboard.setClipboard(s);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            StringSelection selection = new StringSelection(s);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } catch (Throwable ignored) {
        }
    }
}
