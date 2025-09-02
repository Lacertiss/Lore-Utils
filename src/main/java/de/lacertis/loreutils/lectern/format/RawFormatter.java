package de.lacertis.loreutils.lectern.format;

import de.lacertis.loreutils.lectern.LecternCopyOptions;
import de.lacertis.loreutils.lectern.extract.BookExtractor;
import de.lacertis.loreutils.lectern.extract.TextComponentFlattener;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RawFormatter {
    private RawFormatter() {}

    public static String format(BookExtractor.BookSnapshot snap, LecternCopyOptions opt) {
        Objects.requireNonNull(snap, "snap");
        Objects.requireNonNull(opt, "opt");
        StringBuilder out = new StringBuilder();

        if (opt.isIncludeTitleAuthor()) {
            String title = safe(snap.title());
            String author = safe(snap.author());
            if (!title.isEmpty()) {
                out.append(title);
                if (!author.isEmpty()) out.append(" (" ).append(author).append(")");
                out.append("\n\n");
            } else if (!author.isEmpty()) {
                out.append(author).append("\n\n");
            }
        }

        List<String> pageStrings = new ArrayList<>();
        int total = Math.max(0, snap.pageCount());
        for (int i = 0; i < total; i++) {
            Text t = i < snap.pages().size() ? snap.pages().get(i) : Text.literal("");
            String page = renderRawPage(t);
            if (opt.isNumberPages()) {
                pageStrings.add("Page " + (i + 1) + "/" + total + "\n" + page);
            } else {
                pageStrings.add(page);
            }
        }

        String sep = opt.normalizedSeparator();
        for (int i = 0; i < pageStrings.size(); i++) {
            out.append(pageStrings.get(i));
            if (i < pageStrings.size() - 1) out.append(sep);
        }
        String result = out.toString();
        int end = result.length();
        while (end > 0 && Character.isWhitespace(result.charAt(end - 1))) end--;
        return end == result.length() ? result : result.substring(0, end);
    }

    private static String renderRawPage(Text page) {
        List<TextComponentFlattener.Run> runs = TextComponentFlattener.flatten(page);
        StringBuilder sb = new StringBuilder();
        Style prev = null;
        for (TextComponentFlattener.Run run : runs) {
            String delta = emitDelta(prev, run.style());
            if (!delta.isEmpty()) sb.append(delta);
            sb.append(run.text());
            prev = run.style();
        }
        return sb.toString();
    }

    private static String emitStyle(Style s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        TextColor col = s.getColor();
        if (col != null) sb.append('§').append(legacyColorCode(col.getRgb()));
        if (s.isBold()) sb.append('§').append('l');
        if (s.isItalic()) sb.append('§').append('o');
        if (s.isUnderlined()) sb.append('§').append('n');
        if (s.isStrikethrough()) sb.append('§').append('m');
        return sb.toString();
    }

    private static String emitDelta(Style prev, Style next) {
        if (prev == next || (prev != null && prev.equals(next))) return "";
        if (next == null) return "§r";
        StringBuilder sb = new StringBuilder();
        if (prev != null) sb.append('§').append('r');
        sb.append(emitStyle(next));
        return sb.toString();
    }

    private static char legacyColorCode(int rgb) {
        int[] base = {
                0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
                0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };
        char[] codes = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int best = 0; long bestD = Long.MAX_VALUE;
        for (int i = 0; i < base.length; i++) {
            int brgb = base[i];
            int br = (brgb >> 16) & 0xFF, bg = (brgb >> 8) & 0xFF, bb = brgb & 0xFF;
            long dr = r - br, dg = g - bg, db = b - bb;
            long d = dr*dr + dg*dg + db*db;
            if (d < bestD) { bestD = d; best = i; }
        }
        return codes[best];
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
