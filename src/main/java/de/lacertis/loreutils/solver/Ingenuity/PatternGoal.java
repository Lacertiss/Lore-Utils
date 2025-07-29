package de.lacertis.loreutils.solver.Ingenuity;

import java.util.EnumSet;
import java.util.Map;

public class PatternGoal implements Goal {
    private final Map<Slot, EnumSet<Tile>> allowed;

    public PatternGoal(Map<Slot, EnumSet<Tile>> allowed) {
        this.allowed = allowed;
    }

    @Override
    public boolean isSatisfied(Tile[] snap) {
        if (snap == null || snap.length != Slot.values().length) return false;
        Slot[] slots = Slot.values();
        for (int i = 0; i < slots.length; i++) {
            EnumSet<Tile> allowedTiles = allowed.get(slots[i]);
            if (allowedTiles == null || !allowedTiles.contains(snap[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder("PatternGoal[");
        for (Slot slot : Slot.values()) {
            EnumSet<Tile> tiles = allowed.get(slot);
            if (tiles != null) {
                sb.append(slot.name()).append("=").append(tiles.size()).append(",");
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}

