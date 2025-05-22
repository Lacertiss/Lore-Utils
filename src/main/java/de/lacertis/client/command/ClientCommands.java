package de.lacertis.client.command;

import com.mojang.brigadier.CommandDispatcher;
import de.lacertis.client.MessageManager;
import de.lacertis.client.PlayerArea;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("lore")
                        .then(literal("refresh")
                                .then(literal("lightsout")
                                        .executes(ctx -> {
                                            if (!PlayerArea.LIGHTS_OUT.isActive()) {
                                                MessageManager.sendColored("You are not in a lights out area.");
                                                return 1;
                                            }
                                            MessageManager.sendColored("Refreshing lights out...");
                                            LightsOutSolver.Tile[][] grid = LightsOutInput.createGridFromLights(LightsOutInput.createLightStates());
                                            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

                                            switch (config.lightsOutSolverMode) {
                                                case ALL_ON -> {
                                                    List<LightsOutSolver.Pos> solution = LightsOutSolver.solveAllOnOptimized(grid);
                                                    RenderSolvedLightsOut.renderSolution(solution);
                                                }
                                                case ALL_OFF -> {
                                                    List<LightsOutSolver.Pos> solution = LightsOutSolver.solveAllOffOptimized(grid);
                                                    RenderSolvedLightsOut.renderSolution(solution);
                                                }
                                                case STRENGTH -> {
                                                    List<LightsOutSolver.Pos> solution = LightsOutSolver.solveStrengthOptimized(grid);
                                                    RenderSolvedLightsOut.renderSolution(solution);
                                                }
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }
}