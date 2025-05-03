package de.lacertis.client.solver;

import de.lacertis.client.EspRender;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderSolvedPuzzle {

    public static Map<PuzzleSolver.Pos, LightsOutCoords> createMapping() {
        LightsOutCoords[] coords = LightsOutCoords.values();
        Map<PuzzleSolver.Pos, LightsOutCoords> mapping = new HashMap<>();
        int idx = 0;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (PuzzleInput.triggerLayout[x][y]) {
                    mapping.put(new PuzzleSolver.Pos(x, y), coords[idx++]);
                }
            }
        }
        return mapping;
    }

    public static void renderSolution(List<PuzzleSolver.Pos> positions) {
        Map<PuzzleSolver.Pos, LightsOutCoords> mapping = createMapping();
        for (PuzzleSolver.Pos move : positions) {
            LightsOutCoords coord = mapping.get(move);
            if (coord != null) {
                EspRender.registerPosition(new BlockPos((int) coord.getX(), (int) coord.getY() + 1, (int) coord.getZ()));
            }
        }
    }
}