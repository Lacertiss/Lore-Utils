package de.lacertis.loreutils.lectern;

import de.lacertis.loreutils.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.Screen;

public final class LecternCopyButtonController {
    private LecternCopyButtonController() {}

    public static String execute(Screen screen, int currentPage, @javax.annotation.Nullable CopyAction overrideAction, @javax.annotation.Nullable OutputMode overrideMode) {
        try {
            ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            LecternCopyOptions opt = LecternCopyOptions.fromConfigAndContext(cfg, currentPage, overrideAction, overrideMode);
            return switch (opt.getAction()) {
                case PAGE -> LecternCopyService.copyPage(screen, opt);
                case ALL -> LecternCopyService.copyAll(screen, opt);
            };
        } catch (Throwable ignored) {
            return "";
        }
    }
}

