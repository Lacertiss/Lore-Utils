package de.lacertis.client;

public class PuzzleInput {

    private static final boolean[][] triggerLayout = {
            {false, true,  false, true,  false, true,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {false, true,  true,  true,  true,  false,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {false, false, true,  true,  true,  true,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {false, true,  false, true,  false, true,  false}
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
}