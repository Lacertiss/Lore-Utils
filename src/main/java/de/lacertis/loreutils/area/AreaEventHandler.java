package de.lacertis.loreutils.area;

import de.lacertis.loreutils.EspRender;
import de.lacertis.loreutils.MessageManager;
import de.lacertis.loreutils.PlayerArea;
import de.lacertis.loreutils.config.ModConfig;
import de.lacertis.loreutils.solver.Ingenuity.*;
import de.lacertis.loreutils.solver.lightsout.Input;
import de.lacertis.loreutils.solver.lightsout.Solver;
import de.lacertis.loreutils.solver.lightsout.RenderSolvedLightsOut;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AreaEventHandler {

    public static void handleAreaEnter(PlayerArea areaType, boolean enter) {
        if (areaType == PlayerArea.LIGHTS_OUT) {
            if (enter) {
                ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (!cfg.autoSolveLightsOut) return;
                MessageManager.sendActionBarColored("Solving Lights Out: &7" + cfg.lightsOutSolverMode);
                Solver.Tile[][] grid = Input.createGridFromLights(Input.createLightStates());
                List<Solver.Pos> solution = switch (cfg.lightsOutSolverMode) {
                    case ALL_ON -> Solver.solveAllOnOptimized(grid);
                    case ALL_OFF -> Solver.solveAllOffOptimized(grid);
                    case STRENGTH -> Solver.solveStrengthOptimized(grid);
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

        if (areaType == PlayerArea.INGENUITY) {
            if (enter) {
                ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (!cfg.ingenuityTip) return;

                IngenuityPerms d = PermutationsStorage.loadOrDefault();
                boolean ready = PermutationsStorage.permsReady(d);
                Goal goal = PatternGoalIO.loadOrCreateDefault();

                if (!ready) {
                    MessageManager.sendActionBarColored("Ingenuity: &cPerms not learned. Use &e/lore ingenuity calibrate&c.");
                } else if (goal == null) {
                    MessageManager.sendActionBarColored("Ingenuity: &cNo goal configured.");
                } else {
                    MessageManager.sendActionBarColored("Ingenuity: &aReady. Use &b/lore ingenuity solve&a.");
                }

                EspRender.unregisterAllPositions();
                EspRender.registerPosition(new BlockPos(-12156, 72, 12579));
                EspRender.registerPosition(new BlockPos(-12180, 72, 12579));
                EspRender.registerPosition(new BlockPos(-12204, 72, 12579));
            } else {
                EspRender.unregisterAllPositions();
            }
        }
    }
}