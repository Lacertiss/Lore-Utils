package de.lacertis.loreutils.lectern.format;

import de.lacertis.loreutils.lectern.LecternCopyOptions;
import de.lacertis.loreutils.lectern.extract.BookExtractor;
import de.lacertis.loreutils.lectern.extract.LegacyColorNormalizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FormattedFormatter {
    private FormattedFormatter() {}

    public static String format(BookExtractor.BookSnapshot snap, LecternCopyOptions opt) {
        Objects.requireNonNull(snap, "snap");
        Objects.requireNonNull(opt, "opt");
        StringBuilder out = new StringBuilder();

        if (opt.isIncludeTitleAuthor()) {
            String title = safe(snap.title());
            String author = safe(snap.author());
            boolean wrote = false;
            if (!title.isEmpty()) {
                out.append(title).append('\n');
                wrote = true;
            }
            if (!author.isEmpty()) {
                out.append(author).append('\n');
                wrote = true;
            }
            if (wrote) out.append('\n');
        }

        int total = Math.max(0, snap.pageCount());
        List<String> pages = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            Text t = i < snap.pages().size() ? snap.pages().get(i) : Text.literal("");
            String src = t == null ? "" : t.getString();
            src = LegacyColorNormalizer.stripAllFormattingCodes(src);
            List<String> wrappedLines = wrapPlain(src);
            StringBuilder pageOut = new StringBuilder();
            if (opt.isNumberPages()) {
                pageOut.append("Page ").append(i + 1).append('/').append(total).append('\n');
            }
            for (int li = 0; li < wrappedLines.size(); li++) {
                pageOut.append(wrappedLines.get(li));
                if (li < wrappedLines.size() - 1) pageOut.append('\n');
            }
            pages.add(pageOut.toString());
        }

        for (int i = 0; i < pages.size(); i++) {
            out.append(pages.get(i));
            if (i < pages.size() - 1 && opt.isFormattedKeepPageBreaks()) {
                out.append(opt.normalizedSeparator());
            }
        }

        String result = out.toString();
        int end = result.length();
        while (end > 0 && Character.isWhitespace(result.charAt(end - 1))) end--;
        return end == result.length() ? result : result.substring(0, end);
    }

    private static List<String> wrapPlain(String src) {
        var tr = MinecraftClient.getInstance().textRenderer;
        List<String> lines = new ArrayList<>();
        String[] paragraphs = src.split("\n\n", -1);
        for (int p = 0; p < paragraphs.length; p++) {
            String para = paragraphs[p];
            String[] rawLines = para.split("\n", -1);
            for (String rl : rawLines) {
                List<OrderedText> wrapped = tr.wrapLines(Text.literal(rl), 114);
                if (wrapped.isEmpty()) {
                    lines.add("");
                } else {
                    for (OrderedText ot : wrapped) {
                        StringBuilder sb = new StringBuilder();
                        ot.accept((index, style, codePoint) -> { sb.append(Character.toChars(codePoint)); return true; });
                        lines.add(sb.toString());
                    }
                }
            }
            if (p < paragraphs.length - 1) {
                lines.add("");
            }
        }
        return lines;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
