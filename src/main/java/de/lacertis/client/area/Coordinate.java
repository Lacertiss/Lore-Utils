package de.lacertis.client.area;

import net.minecraft.util.math.BlockPos;

public enum Coordinate {
    ANUAR_1(new BlockPos(-11256, 19, 12599)),
    ANUAR_2(new BlockPos(-11238, 27, 12581)),
    ANUAR_3(new BlockPos(-11256, 35, 12599)),
    ANUAR_4(new BlockPos(-11238, 51, 12581));

    private final BlockPos pos;

    Coordinate(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}