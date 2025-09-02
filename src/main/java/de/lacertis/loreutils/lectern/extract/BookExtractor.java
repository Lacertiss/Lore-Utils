package de.lacertis.loreutils.lectern.extract;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BookExtractor {
    private BookExtractor() {}

    public static boolean isLecternScreen(Screen screen) {
        return screen instanceof LecternScreen;
    }

    public static BookSnapshot fromScreen(Screen screen) {
        if (!(screen instanceof LecternScreen lectern)) {
            return new BookSnapshot("", "", 0, Collections.emptyList());
        }
        ItemStack stack = resolveBookStack(lectern);
        if (stack == null || stack.isEmpty()) {
            return new BookSnapshot("", "", 0, Collections.emptyList());
        }
        String title = safeGetTitle(stack);
        String author = safeGetAuthor(stack);
        List<Text> pages = safeReadPages(stack);
        int pageCount = pages.size();
        return new BookSnapshot(nonNull(title), nonNull(author), pageCount, Collections.unmodifiableList(pages));
    }

    private static ItemStack resolveBookStack(LecternScreen lectern) {
        try {
            LecternScreenHandler handler = lectern.getScreenHandler();
            if (handler != null) {
                return handler.getBookItem();
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private static String safeGetTitle(ItemStack stack) {
        try {
            Object content = getWrittenBookComponent(stack);
            if (content != null) {
                Method m = content.getClass().getMethod("title");
                Object v = m.invoke(content);
                if (v instanceof String s && !s.isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        try {
            Text name = stack.getName();
            return name != null ? name.getString() : "";
        } catch (Throwable ignored) {}
        return "";
    }

    private static String safeGetAuthor(ItemStack stack) {
        try {
            Object content = getWrittenBookComponent(stack);
            if (content != null) {
                Method m = content.getClass().getMethod("author");
                Object v = m.invoke(content);
                if (v instanceof String s) return s;
            }
        } catch (Throwable ignored) {}
        return "";
    }

    private static Object getWrittenBookComponent(ItemStack stack) {
        try {
            Class<?> dctClass = Class.forName("net.minecraft.component.DataComponentTypes");
            Object key = null;
            for (Field f : dctClass.getDeclaredFields()) {
                String n = f.getName();
                if (n != null && n.toUpperCase().contains("WRITTEN") && n.toUpperCase().contains("BOOK")) {
                    key = f.get(null);
                    break;
                }
            }
            if (key == null) return null;
            for (Method m : ItemStack.class.getMethods()) {
                if (m.getName().equals("get") && m.getParameterCount() == 1) {
                    Class<?> p0 = m.getParameterTypes()[0];
                    if (p0.getName().contains("DataComponentType")) {
                        return m.invoke(stack, key);
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static List<Text> safeReadPages(ItemStack stack) {
        try {
            BookScreen.Contents contents = BookScreen.Contents.create(stack);
            if (contents != null) {
                int count = contents.getPageCount();
                if (count > 0) {
                    List<Text> pages = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        var sv = contents.getPage(i);
                        Text t = sv != null ? Text.literal(sv.getString()) : Text.literal("");
                        pages.add(t);
                    }
                    return pages;
                }
            }
        } catch (Throwable ignored) {}
        return new ArrayList<>();
    }

    private static Slot safeGetSlot(ScreenHandler handler, int index) {
        try { return handler.getSlot(index); } catch (Throwable e) { return null; }
    }

    private static String nonNull(String s) { return s == null ? "" : s; }

    public static record BookSnapshot(String title, String author, int pageCount, List<Text> pages) {
        public boolean hasBook() {
            return pageCount > 0 || (title != null && !title.isEmpty()) || (author != null && !author.isEmpty());
        }
        public List<Text> pages() {
            return Objects.requireNonNullElseGet(pages, List::of);
        }
    }
}
