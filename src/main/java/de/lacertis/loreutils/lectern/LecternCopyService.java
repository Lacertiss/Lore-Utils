package de.lacertis.loreutils.lectern;

import de.lacertis.loreutils.data.FileManager;
import de.lacertis.loreutils.lectern.extract.BookExtractor;
import de.lacertis.loreutils.lectern.format.FormattedFormatter;
import de.lacertis.loreutils.lectern.format.RawFormatter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LecternCopyService {
    private LecternCopyService() {}

    public static String copyPage(Screen screen, LecternCopyOptions opt) {
        if (screen == null || opt == null || !BookExtractor.isLecternScreen(screen)) return "";
        BookExtractor.BookSnapshot snap = BookExtractor.fromScreen(screen);
        if (snap == null || !snap.hasBook()) return "";
        int idx = Math.max(0, opt.getPageIndex());
        BookExtractor.BookSnapshot single = restrictToPage(snap, idx);
        String content = format(single, opt);
        pushResults(opt, single, content);
        return content;
    }

    public static String copyAll(Screen screen, LecternCopyOptions opt) {
        if (screen == null || opt == null || !BookExtractor.isLecternScreen(screen)) return "";
        BookExtractor.BookSnapshot snap = BookExtractor.fromScreen(screen);
        if (snap == null || !snap.hasBook()) return "";
        String content = format(snap, opt);
        pushResults(opt, snap, content);
        return content;
    }

    private static BookExtractor.BookSnapshot restrictToPage(BookExtractor.BookSnapshot snap, int index) {
        List<Text> pages = snap.pages();
        if (pages == null || pages.isEmpty()) {
            return new BookExtractor.BookSnapshot(snap.title(), snap.author(), 0, Collections.emptyList());
        }
        int i = Math.max(0, Math.min(index, pages.size() - 1));
        List<Text> one = new ArrayList<>(1);
        one.add(pages.get(i));
        return new BookExtractor.BookSnapshot(snap.title(), snap.author(), 1, Collections.unmodifiableList(one));
    }

    private static String format(BookExtractor.BookSnapshot snap, LecternCopyOptions opt) {
        return switch (opt.getMode()) {
            case RAW -> RawFormatter.format(snap, opt);
            case FORMATTED -> FormattedFormatter.format(snap, opt);
        };
    }

    private static void pushResults(LecternCopyOptions opt, BookExtractor.BookSnapshot snap, String content) {
        if (content == null) content = "";
        de.lacertis.loreutils.util.ClipboardUtil.setClipboard(content);
        if (opt.isExportToFile()) {
            try {
                String ext = opt.fileExtension();
                String sha1 = de.lacertis.loreutils.util.HashingUtil.sha1(content);
                String name = opt.getFilePattern()
                        .replace("{title}", safe(snap.title()))
                        .replace("{author}", safe(snap.author()))
                        .replace("{sha1}", sha1)
                        .replace("{ext}", ext);
                name = FileManager.sanitizeFileName(name);
                FileManager.saveTextInSubfolder("books", name, content);
            } catch (Exception ignored) {
            }
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
