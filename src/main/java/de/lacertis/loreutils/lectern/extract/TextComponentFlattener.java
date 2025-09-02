package de.lacertis.loreutils.lectern.extract;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class TextComponentFlattener {
    private TextComponentFlattener() {}

    public record Run(String text, Style style) { }

    public static List<Run> flatten(Text root) {
        List<Run> out = new ArrayList<>();
        if (root == null) {
            out.add(new Run("", Style.EMPTY));
            return out;
        }
        try {
            OrderedText ordered = root.asOrderedText();
            StringBuilder sb = new StringBuilder();
            final Style[] current = new Style[]{null};
            boolean ok = ordered.accept((index, style, codePoint) -> {
                if (current[0] == null || !current[0].equals(style)) {
                    if (sb.length() > 0) {
                        out.add(new Run(sb.toString(), current[0] == null ? Style.EMPTY : current[0]));
                        sb.setLength(0);
                    }
                    current[0] = style;
                }
                sb.append(Character.toChars(codePoint));
                return true;
            });
            if (sb.length() > 0) {
                out.add(new Run(sb.toString(), current[0] == null ? Style.EMPTY : current[0]));
            }
            if (ok && !out.isEmpty()) {
                return out;
            }
        } catch (Throwable ignored) {
        }
        out.clear();
        out.add(new Run(root.getString(), root.getStyle() == null ? Style.EMPTY : root.getStyle()));
        return out;
    }
}

