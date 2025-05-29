package de.lacertis.client.area;

import de.lacertis.client.EspRender;
import de.lacertis.client.MessageManager;
import de.lacertis.client.PlayerArea;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import de.lacertis.client.solver.LightsOutSolverMode;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.List;

public class AreaEventHandler {

    public static void handleAreaEnter(PlayerArea areaType) {
        if (areaType == PlayerArea.LIGHTS_OUT) {
            if (!AutoConfig.getConfigHolder(ModConfig.class).getConfig().AutoSolveLightsOut) {
                return;
            }
            MessageManager.sendColored("Solving Lights Out: &7" +
                    AutoConfig.getConfigHolder(ModConfig.class).getConfig().lightsOutSolverMode);
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
}