package de.lacertis.client;

public enum Coordinates {
    COORD_1(-11258.5, 35, 12683.5),
    COORD_2(-11258.5, 35, 12669.5),
    COORD_3(-11258.5, 35, 12655.5),

    COORD_4(-11251.5, 35, 12690.5),
    COORD_5(-11251.5, 35, 12683.5),
    COORD_6(-11251.5, 35, 12676.5),
    COORD_7(-11251.5, 35, 12669.5),
    COORD_8(-11251.5, 35, 12662.5),
    COORD_9(-11251.5, 35, 12655.5),
    COORD_10(-11251.5, 35, 12648.5),

    COORD_11(-11244.5, 35, 12683.5),
    COORD_12(-11244.5, 35, 12676.5),
    COORD_13(-11244.5, 35, 12669.5),
    COORD_14(-11244.5, 35, 12662.5),

    COORD_15(-11237.5, 35, 12690.5),
    COORD_16(-11237.5, 35, 12683.5),
    COORD_17(-11237.5, 35, 12676.5),
    COORD_18(-11237.5, 35, 12669.5),
    COORD_19(-11237.5, 35, 12662.5),
    COORD_20(-11237.5, 35, 12655.5),
    COORD_21(-11237.5, 35, 12648.5),

    COORD_22(-11230.5, 35, 12676.5),
    COORD_23(-11230.5, 35, 12669.5),
    COORD_24(-11230.5, 35, 12662.5),
    COORD_25(-11230.5, 35, 12655.5),

    COORD_26(-11223.5, 35, 12690.5),
    COORD_27(-11223.5, 35, 12683.5),
    COORD_28(-11223.5, 35, 12676.5),
    COORD_29(-11223.5, 35, 12669.5),
    COORD_30(-11223.5, 35, 12662.5),
    COORD_31(-11223.5, 35, 12655.5),
    COORD_32(-11223.5, 35, 12648.5),

    COORD_33(-11216.5, 35, 12683.5),
    COORD_34(-11216.5, 35, 12669.5),
    COORD_35(-11216.5, 35, 12655.5);

    private final double x;
    private final double y;
    private final double z;

    Coordinates(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}