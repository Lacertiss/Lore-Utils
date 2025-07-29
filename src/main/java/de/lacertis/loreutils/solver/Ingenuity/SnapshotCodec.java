package de.lacertis.loreutils.solver.Ingenuity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class SnapshotCodec {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private SnapshotCodec() { }

    public static String encode(Tile[] snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("SNAPSHOT_NULL");
        if (snapshot.length != Slot.values().length)
            throw new IllegalArgumentException("SNAPSHOT_SIZE_MISMATCH expected=" + Slot.values().length + " got=" + snapshot.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < snapshot.length; i++) {
            Tile t = snapshot[i];
            if (t == null) throw new IllegalArgumentException("SNAPSHOT_TILE_NULL idx=" + i);
            sb.append(t.name());
            if (i < snapshot.length - 1) sb.append('|');
        }
        return sb.toString();
    }

    public static Tile[] decode(String s) {
        if (s == null) throw new IllegalArgumentException("STRING_NULL");
        String[] parts = s.split("\\|", -1);
        if (parts.length != Slot.values().length)
            throw new IllegalArgumentException("SNAPSHOT_PARTS_MISMATCH expected=" + Slot.values().length + " got=" + parts.length);
        Tile[] out = new Tile[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Tile.valueOf(parts[i]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("INVALID_TILE \"" + parts[i] + "\" at idx=" + i, e);
            }
        }
        return out;
    }

    public static String hash(Tile[] snapshot) {
        String encoded = encode(snapshot);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(encoded.getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int v = digest[i] & 0xFF;
                hex[i * 2] = HEX[v >>> 4];
                hex[i * 2 + 1] = HEX[v & 0x0F];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA1_ALGORITHM_MISSING", e);
        }
    }
}
