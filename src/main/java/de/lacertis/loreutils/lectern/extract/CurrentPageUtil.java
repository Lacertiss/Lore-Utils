package de.lacertis.loreutils.lectern.extract;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.screen.ScreenHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CurrentPageUtil {
    private CurrentPageUtil() {}

    public static int getCurrentPage(Screen screen) {
        if (!(screen instanceof LecternScreen lectern)) return 0;
        try {
            ScreenHandler handler = lectern.getScreenHandler();
            if (handler != null) {
                Integer val = tryCallIntNoArgs(handler, "getPage");
                if (val == null) val = tryCallIntNoArgs(handler, "getCurrentPage");
                if (val == null) val = tryCallIntNoArgs(handler, "getPageIndex");
                if (val != null) return clamp(val);

                try {
                    Method m = handler.getClass().getMethod("getProperty", int.class);
                    Object v = m.invoke(handler, 0);
                    if (v instanceof Integer i) return clamp(i);
                } catch (Throwable ignored) {}

                try {
                    Method mDel = handler.getClass().getMethod("getProperties");
                    Object delegate = mDel.invoke(handler);
                    if (delegate != null) {
                        try {
                            Method mGet = delegate.getClass().getMethod("get", int.class);
                            Object v = mGet.invoke(delegate, 0);
                            if (v instanceof Integer i) return clamp(i);
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}

                Integer f = tryReadIntField(handler, "page");
                if (f == null) f = tryReadIntField(handler, "currentPage");
                if (f != null) return clamp(f);
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    private static Integer tryCallIntNoArgs(Object o, String name) {
        try {
            Method m = o.getClass().getMethod(name);
            Object v = m.invoke(o);
            if (v instanceof Integer i) return i;
        } catch (Throwable ignored) {}
        return null;
    }

    private static Integer tryReadIntField(Object o, String name) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Object v = f.get(o);
            if (v instanceof Integer i) return i;
        } catch (Throwable ignored) {}
        return null;
    }

    private static int clamp(int v) { return Math.max(0, v); }
}

