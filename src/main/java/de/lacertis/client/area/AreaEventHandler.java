package de.lacertis.client.area;

import de.lacertis.client.EspRender;
import de.lacertis.client.MessageManager;
import de.lacertis.client.PlayerArea;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.List;

public class AreaEventHandler {

    public static void handleAreaEnter(PlayerArea areaType, boolean enter) {
        if (areaType == PlayerArea.LIGHTS_OUT) {
            if (enter) {
                ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (!cfg.autoSolveLightsOut) return;
                MessageManager.sendActionBarColored("Solving Lights Out: &7" + cfg.lightsOutSolverMode);
                LightsOutSolver.Tile[][] grid = LightsOutInput.createGridFromLights(LightsOutInput.createLightStates());
                List<LightsOutSolver.Pos> solution = switch (cfg.lightsOutSolverMode) {
                    case ALL_ON -> LightsOutSolver.solveAllOnOptimized(grid);
                    case ALL_OFF -> LightsOutSolver.solveAllOffOptimized(grid);
                    case STRENGTH -> LightsOutSolver.solveStrengthOptimized(grid);
                };
                RenderSolvedLightsOut.renderSolution(solution);
            } else {
                EspRender.unregisterAllPositions();
            }
            return;
        }

        if (areaType == PlayerArea.ANUAR_GEM) {
            if (enter) {
                ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (!cfg.autoMarkAnuarButtons) return;
                MessageManager.sendActionBarColored("Marking Anuar Gem Buttons...");
                EspRender.registerPosition(Coordinate.ANUAR_1.getPos());
                EspRender.registerPosition(Coordinate.ANUAR_2.getPos());
                EspRender.registerPosition(Coordinate.ANUAR_3.getPos());
                EspRender.registerPosition(Coordinate.ANUAR_4.getPos());
            } else {
                EspRender.unregisterAllPositions();
            }
        }
    }
}