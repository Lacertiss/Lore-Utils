package de.lacertis.loreutils.solver.lightsout;

import de.lacertis.loreutils.EspRender;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenderSolvedLightsOut {

    public static Map<Solver.Pos, Coords> createMapping() {
        Coords[] coords = Coords.values();
        Map<Solver.Pos, Coords> mapping = new HashMap<>();
        int idx = 0;
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (Input.triggerLayout[x][y]) {
                    mapping.put(new Solver.Pos(x, y), coords[idx++]);
                }
            }
        }
        return mapping;
    }

    public static void renderSolution(List<Solver.Pos> positions) {
        Map<Solver.Pos, Coords> mapping = createMapping();
        Set<BlockPos> highlights = new HashSet<>();

        EspRender.unregisterAllPositions();

        for (Solver.Pos move : positions) {
            Coords coord = mapping.get(move);
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