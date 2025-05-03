package de.lacertis.client;

import java.util.Arrays;

public class PuzzleInput {

    public static final boolean[][] triggerLayout = {
            {false, true, false, true, false, true, false},
            {true, true, true, true, true, true, true},
            {false, true, true, true, true, false, false},
            {true, true, true, true, true, true, true},
            {false, false, true, true, true, true, false},
            {true, true, true, true, true, true, true},
            {false, true, false, true, false, true, false}
    };

    public static PuzzleSolver.Tile[][] createGridFromLights(boolean[][] lightStates) {
        PuzzleSolver.Tile[][] grid = new PuzzleSolver.Tile[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                grid[x][y] = new PuzzleSolver.Tile(triggerLayout[x][y], lightStates[x][y]);
            }
        }
        return grid;
    }

    public static boolean[][] createLightStates() {
        boolean[][] lightStates = new boolean[7][7];
        LightsOutCoords[] coords = LightsOutCoords.values();

        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                int index = x * 7 + y;
                if (index < coords.length && triggerLayout[x][y]) {
                    LightsOutCoords c = coords[index];
                    boolean lampBelow = isLampPowered((int) c.getX(), (int) c.getY() - 1, (int) c.getZ());
                    boolean lampEast  = isLampPowered((int) c.getX() + 1, (int) c.getY(), (int) c.getZ());
                    lightStates[x][y] = lampBelow || lampEast;
                } else {
                    lightStates[x][y] = false;
                }
            }
        }
        for (boolean[] row : lightStates) {
            System.out.println(Arrays.toString(row));
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
        return blockState.getBlock() instanceof net.minecraft.block.RedstoneLampBlock
                && blockState.get(net.minecraft.state.property.Properties.LIT);
    }

}