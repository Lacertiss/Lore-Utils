package de.lacertis.loreutils.solver.Ingenuity;

public enum Move {
    EAST(0),
    WEST(1),
    LECTERN(2),
    UNKNOWN(-1);

    public final int index;

    Move(int index) {
        this.index = index;
    }

    public static Move parse(String s) {
        if (s == null) return null;
        switch (s.trim().toUpperCase()) {
            case "EAST": return EAST;
            case "WEST": return WEST;
            case "LECTERN": return LECTERN;
            case "UNKNOWN": return UNKNOWN;
            default: return null;
        }
    }

    public boolean isLectern() {
        return this == LECTERN;
    }
}