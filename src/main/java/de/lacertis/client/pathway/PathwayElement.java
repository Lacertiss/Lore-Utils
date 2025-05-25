package de.lacertis.client.pathway;

import net.minecraft.util.math.BlockPos;

public class PathwayElement {
    private PathwayType type;
    private BlockPos pos1;
    private BlockPos pos2;

    public PathwayElement() {
    }

    public PathwayElement(PathwayType type, BlockPos pos1, BlockPos pos2) {
        this.type = type;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public PathwayType getType() {
        return type;
    }

    public void setType(PathwayType type) {
        this.type = type;
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }
}