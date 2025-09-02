package de.lacertis.loreutils.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashingUtil {
    private HashingUtil() {}

    //SHA-1 Hex (lower-case).
    public static String sha1(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] dig = md.digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit((b) & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

