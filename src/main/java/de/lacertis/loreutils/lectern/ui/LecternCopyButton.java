package de.lacertis.loreutils.lectern.ui;

import de.lacertis.loreutils.MessageManager;
import de.lacertis.loreutils.lectern.LecternCopyButtonController;
import de.lacertis.loreutils.lectern.extract.CurrentPageUtil;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class LecternCopyButton {
    private LecternCopyButton() {}

    public static void attachTo(Screen screen) {
        if (!(screen instanceof LecternScreen)) return;
        try {
            int btnW = 52, btnH = 20;
            int x = screen.width - btnW - 8;
            int y = 8;

            ButtonWidget button = ButtonWidget.builder(Text.literal("Copy"), b -> {
                int currentPage = 0;
                try {
                    currentPage = CurrentPageUtil.getCurrentPage(screen);
                } catch (Throwable ignored) {}
                String result = "";
                try {
                    result = LecternCopyButtonController.execute(screen, currentPage, null, null);
                } catch (Throwable ignored) {}
                if (result == null || result.isEmpty()) {
                    MessageManager.sendActionBarColored("No book in lectern");
                } else {
                    MessageManager.sendActionBarColored("Copied (" + result.length() + " chars)");
                }
            }).dimensions(x, y, btnW, btnH).build();

            Screens.getButtons(screen).add(button);
        } catch (Throwable ignored) {
        }
    }
}
