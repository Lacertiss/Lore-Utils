package de.lacertis.loreutils.solver.Ingenuity;

import de.lacertis.loreutils.MessageManager;
import de.lacertis.loreutils.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class IngenuityDebug {

    public static boolean enabled() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig().ingenuityDebug;
    }

    public static void chat(String fmt, Object... args) {
        if (!enabled()) return;
        MessageManager.sendChatColored("&b[IngenuityDebug]&7 " + String.format(fmt, args));
    }

    public static void bar(String fmt, Object... args) {
        if (!enabled()) return;
        MessageManager.sendActionBarColored("&b[IngenuityDebug]&7 " + String.format(fmt, args));
    }

    public static String hash(Tile[] snap) {
        return SnapshotCodec.hash(snap);
    }

    public static String slots(EnumSet<Slot> set) {
        return set.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
