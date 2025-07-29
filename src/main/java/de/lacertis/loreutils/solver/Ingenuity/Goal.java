package de.lacertis.loreutils.solver.Ingenuity;

public interface Goal {
    boolean isSatisfied(Tile[] snap);
    String describe();
}
