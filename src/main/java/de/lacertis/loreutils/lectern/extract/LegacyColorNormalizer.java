package de.lacertis.loreutils.lectern.extract;

public final class LegacyColorNormalizer {
    private LegacyColorNormalizer() {}

    public static String stripObfuscationCodes(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        StringBuilder out = new StringBuilder(s.length());
        boolean obfActive = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(i + 1));
                if (code == 'k') {
                    obfActive = true;
                    i++;
                    continue;
                }
                if (isFormatCode(code)) {
                    if (obfActive) {
                        obfActive = code == 'k';
                        i++;
                        continue;
                    }
                    out.append(c).append(s.charAt(i + 1));
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    public static String stripAllFormattingCodes(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                char code = s.charAt(i + 1);
                if (isFormatCode(code)) {
                    i++; // skip both
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    public static String revealObfuscated(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        StringBuilder out = new StringBuilder(s.length());
        boolean obfActive = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(i + 1));
                if (code == 'k') {
                    obfActive = true;
                    i++;
                    continue;
                }
                if (isFormatCode(code)) {
                    obfActive = false;
                    out.append('§').append(s.charAt(i + 1));
                    i++;
                    continue;
                }
            }
            if (obfActive && !isFormatPrefixAt(s, i)) {
                out.append('■');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean isFormatPrefixAt(String s, int i) {
        return s.charAt(i) == '§' && (i + 1 < s.length()) && isFormatCode(s.charAt(i + 1));
    }

    private static boolean isFormatCode(char code) {
        code = Character.toLowerCase(code);
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f') || code == 'k' || code == 'l' || code == 'm' || code == 'n' || code == 'o' || code == 'r';
    }
}

