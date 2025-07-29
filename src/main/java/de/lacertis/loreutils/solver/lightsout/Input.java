package de.lacertis.loreutils.solver.lightsout;

public class Input {

    public static final boolean[][] triggerLayout = {
            {false, true, false, true, false, true, false},
            {true, true, true, true, true, true, true},
            {false, true, true, true, true, false, false},
            {true, true, true, true, true, true, true},
            {false, false, true, true, true, true, false},
            {true, true, true, true, true, true, true},
            {false, true, false, true, false, true, false}
    };

    public static Solver.Tile[][] createGridFromLights(boolean[][] lightStates) {
        Solver.Tile[][] grid = new Solver.Tile[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                grid[x][y] = new Solver.Tile(triggerLayout[x][y], lightStates[x][y]);
            }
        }
        return grid;
    }

    public static boolean[][] createLightStates() {
        boolean[][] lightStates = new boolean[7][7];
        Coords[] coords = Coords.values();
        int idx = 0;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (triggerLayout[x][y]) {
                    Coords c = coords[idx++];
                    int bx = (int) c.getX();
                    int by = (int) c.getY();
                    int bz = (int) c.getZ();
                    boolean north = isLampPowered(bx, by, bz - 1);
                    boolean south = isLampPowered(bx, by, bz + 1);
                    boolean east  = isLampPowered(bx + 1, by, bz);
                    boolean west  = isLampPowered(bx - 1, by, bz);
                    lightStates[x][y] = north || south || east || west;
                } else {
                    lightStates[x][y] = false;
                }
            }
        }
        return lightStates;
    }

    public static boolean isLampPowered(int x, int y, int z) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return false;
        }
        var blockPos = new net.minecraft.util.math.BlockPos(x, y, z);
        var blockState = client.world.getBlockState(blockPos);
        return blockState.getBlock() instanceof net.minecraft.block.RedstoneLampBlock && blockState.get(net.minecraft.state.property.Properties.LIT);
    }

}