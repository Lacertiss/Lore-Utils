package de.lacertis.loreutils.solver.Ingenuity;

public enum IngenuityCoords {
    EASTEAST(-12150, 71, 12579),
    EASTNORTH(-12156, 71, 12573),
    EASTWEST(-12162, 71, 12579),
    EASTSOUTH(-12156, 71, 12585),
    WESTWEST(-12210, 71, 12579),
    WESTNORTH(-12204, 71, 12573),
    WESTEAST(-12198, 71, 12579),
    WESTSOUTH(-12204, 71, 12585);

    private final double x;
    private final double y;
    private final double z;

    IngenuityCoords(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
