// Datei: src/main/java/de/lacertis/client/AreaChecker.java
package de.lacertis.client;

import de.lacertis.client.config.ModConfig;
import de.lacertis.client.solver.PuzzleInput;
import de.lacertis.client.solver.PuzzleSolver;
import de.lacertis.client.solver.RenderSolvedPuzzle;
import de.lacertis.client.solver.SolverMode;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AreaChecker {
    private static final List<AreaEntry> AREAS = new ArrayList<>();
    private static boolean initialized = false;

    public static void addArea(Box area, PlayerArea areaType) {
        AREAS.add(new AreaEntry(area, areaType));
    }

    public static void init() {
        if (initialized) return;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            Vec3d playerPos = client.player.getPos();
            for (AreaEntry entry : AREAS) {
                boolean contains = entry.area.contains(playerPos);
                boolean wasActive = entry.areaType.isActive();

                if (contains && !wasActive) {
                    entry.areaType.setActive(true);
                    onAreaEnter(entry.areaType);
                } else if (!contains && wasActive) {
                    entry.areaType.setActive(false);
                    onAreaExit(entry.areaType);
                }
            }
        });

        initialized = true;
    }

    public static void uninit() {
        AREAS.clear();
        initialized = false;
    }

    private static void onAreaEnter(PlayerArea areaType) {
        System.out.println("Entered area: " + areaType);

        if (areaType == PlayerArea.LIGHTS_OUT) {
            if (!AutoConfig.getConfigHolder(ModConfig.class).getConfig().AutoSolveLightsOut) {
                return;
            }
            PuzzleSolver.Tile[][] grid = PuzzleInput.createGridFromLights(PuzzleInput.createLightStates());
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().solverMode == SolverMode.ALL_ON) {
                List<PuzzleSolver.Pos> solution = PuzzleSolver.solveAllOnOptimized(grid);
                RenderSolvedPuzzle.renderSolution(solution);
            }
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().solverMode == SolverMode.ALL_OFF) {
                List<PuzzleSolver.Pos> solution = PuzzleSolver.solveAllOffOptimized(grid);
                RenderSolvedPuzzle.renderSolution(solution);
            }
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().solverMode == SolverMode.STRENGTH) {
                List<PuzzleSolver.Pos> solution = PuzzleSolver.solveStrengthOptimized(grid);
                RenderSolvedPuzzle.renderSolution(solution);
            }
        }
    }

    private static void onAreaExit(PlayerArea areaType) {
        System.out.println("Exited area: " + areaType);
        if (areaType == PlayerArea.LIGHTS_OUT) {
            EspRender.unregisterAllPositions();
        }
    }

    private static class AreaEntry {
        Box area;
        PlayerArea areaType;

        AreaEntry(Box area, PlayerArea areaType) {
            this.area = area;
            this.areaType = areaType;
        }
    }
}