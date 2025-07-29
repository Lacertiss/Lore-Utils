package de.lacertis.loreutils.solver.Ingenuity;

import java.util.EnumSet;

public final class DeltaUtil {
    private static final int MIN_DELTA_SIZE = 1;

    public static EnumSet<Slot> diff(Tile[] a, Tile[] b) {
        if (a == null || b == null) throw new IllegalArgumentException("NULL_SNAPSHOT");
        if (a.length != Slot.values().length || b.length != Slot.values().length)
            throw new IllegalArgumentException("SIZE_MISMATCH");
        EnumSet<Slot> changed = EnumSet.noneOf(Slot.class);
        Slot[] slots = Slot.values();
        for (int i = 0; i < slots.length; i++) if (a[i] != b[i]) changed.add(slots[i]);
        return changed;
    }

    public static Move classify(EnumSet<Slot> delta) {
        if (delta == null || delta.size() < MIN_DELTA_SIZE) return Move.UNKNOWN;

        EnumSet<Slot> right  = EnumSet.of(Slot.EASTNORTH, Slot.EASTEAST, Slot.EASTSOUTH, Slot.EASTWEST);
        EnumSet<Slot> left   = EnumSet.of(Slot.WESTNORTH, Slot.WESTWEST, Slot.WESTSOUTH, Slot.WESTEAST);
        EnumSet<Slot> lectern = EnumSet.of(Slot.WESTWEST, Slot.EASTWEST, Slot.WESTEAST, Slot.EASTEAST);

        if (right.containsAll(delta))   return Move.EAST;
        if (left.containsAll(delta))    return Move.WEST;
        if (lectern.containsAll(delta)) return Move.LECTERN;
        return Move.UNKNOWN;
    }

    private DeltaUtil() { }
}
