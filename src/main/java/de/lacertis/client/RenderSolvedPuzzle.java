package de.lacertis.client;

import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderSolvedPuzzle {

    public static Map<PuzzleSolver.Pos, LightsOutCoords> createMapping() {
        LightsOutCoords[] coords = LightsOutCoords.values();
        Map<PuzzleSolver.Pos, LightsOutCoords> mapping = new HashMap<>();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                int index = x * 7 + y;
                if (index < coords.length && PuzzleInput.triggerLayout[x][y]) {
                    mapping.put(new PuzzleSolver.Pos(x, y), coords[index]);
                }
            }
        }
        return mapping;
    }

    public static void renderSolution(PuzzleSolver.Tile[][] grid) {
        Map<PuzzleSolver.Pos, LightsOutCoords> mapping = createMapping();
        List<PuzzleSolver.Pos> solution = PuzzleSolver.solveAllOnOptimized(grid);
        for (PuzzleSolver.Pos move : solution) {
            LightsOutCoords coord = mapping.get(move);
            if (coord != null) {
                EspRender.registerPosition(new BlockPos((int) coord.getX(), (int) coord.getY(), (int) coord.getZ()));
            }
        }
    }

    public static void renderSolution(List<PuzzleSolver.Pos> positions) {
        Map<PuzzleSolver.Pos, LightsOutCoords> mapping = createMapping();
        for (PuzzleSolver.Pos move : positions) {
            LightsOutCoords coord = mapping.get(move);
            if (coord != null) {
                EspRender.registerPosition(new BlockPos((int) coord.getX(), (int) coord.getY(), (int) coord.getZ()));
            }
        }
    }
}