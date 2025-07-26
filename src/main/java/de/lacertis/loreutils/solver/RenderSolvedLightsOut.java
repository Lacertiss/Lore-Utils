package de.lacertis.loreutils.solver;

import de.lacertis.loreutils.EspRender;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenderSolvedLightsOut {

    public static Map<LightsOutSolver.Pos, LightsOutCoords> createMapping() {
        LightsOutCoords[] coords = LightsOutCoords.values();
        Map<LightsOutSolver.Pos, LightsOutCoords> mapping = new HashMap<>();
        int idx = 0;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (LightsOutInput.triggerLayout[x][y]) {
                    mapping.put(new LightsOutSolver.Pos(x, y), coords[idx++]);
                }
            }
        }
        return mapping;
    }

    public static void renderSolution(List<LightsOutSolver.Pos> positions) {
        Map<LightsOutSolver.Pos, LightsOutCoords> mapping = createMapping();
        Set<BlockPos> highlights = new HashSet<>();

        EspRender.unregisterAllPositions();

        for (LightsOutSolver.Pos move : positions) {
            LightsOutCoords coord = mapping.get(move);
            if (coord != null) {
                BlockPos pos = new BlockPos((int) coord.getX(), (int) coord.getY() + 1, (int) coord.getZ());
                EspRender.registerPosition(pos);
                highlights.add(pos);
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            BlockPos playerPos = client.player.getBlockPos();
            if (highlights.remove(playerPos)) {
                EspRender.unregisterPosition(playerPos);
            }
        });
    }
}