package de.lacertis.client;

import de.lacertis.client.config.ModConfig;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import de.lacertis.client.solver.LightsOutSolverMode;
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
        if (areaType == PlayerArea.LIGHTS_OUT) {
            if (!AutoConfig.getConfigHolder(ModConfig.class).getConfig().AutoSolveLightsOut) {
                return;
            }
            MessageManager.sendColored("Solving Lights Out: &7" + AutoConfig.getConfigHolder(ModConfig.class).getConfig().lightsOutSolverMode);
            LightsOutSolver.Tile[][] grid = LightsOutInput.createGridFromLights(LightsOutInput.createLightStates());
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().lightsOutSolverMode == LightsOutSolverMode.ALL_ON) {
                List<LightsOutSolver.Pos> solution = LightsOutSolver.solveAllOnOptimized(grid);
                RenderSolvedLightsOut.renderSolution(solution);
            }
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().lightsOutSolverMode == LightsOutSolverMode.ALL_OFF) {
                List<LightsOutSolver.Pos> solution = LightsOutSolver.solveAllOffOptimized(grid);
                RenderSolvedLightsOut.renderSolution(solution);
            }
            if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().lightsOutSolverMode == LightsOutSolverMode.STRENGTH) {
                List<LightsOutSolver.Pos> solution = LightsOutSolver.solveStrengthOptimized(grid);
                RenderSolvedLightsOut.renderSolution(solution);
            }
        }

        if (areaType == PlayerArea.ANUAR_GEM) {
            EspRender.registerPosition(Coordinate.ANUAR_1.getPos());
            EspRender.registerPosition(Coordinate.ANUAR_2.getPos());
            EspRender.registerPosition(Coordinate.ANUAR_3.getPos());
            EspRender.registerPosition(Coordinate.ANUAR_4.getPos());
        }
    }

    private static void onAreaExit(PlayerArea areaType) {
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