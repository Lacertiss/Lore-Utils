package de.lacertis.client.command;

import com.mojang.brigadier.CommandDispatcher;
import de.lacertis.client.MessageManager;
import de.lacertis.client.PlayerArea;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.data.Explain;
import de.lacertis.client.data.FileManager;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
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
                        .then(literal("create")
                                .then(literal("explain")
                                        .then(argument("alias", word())
                                                .then(argument("author", word())
                                                        .then(argument("message", greedyString())
                                                                .executes(ctx -> {
                                                                    String alias = ctx.getArgument("alias", String.class);
                                                                    String author = ctx.getArgument("author", String.class);
                                                                    String message = ctx.getArgument("message", String.class);

                                                                    Path configDir = FabricLoader.getInstance()
                                                                            .getConfigDir()
                                                                            .resolve("loreutils")
                                                                            .resolve("explain");
                                                                    Path filePath = configDir.resolve(alias + ".json");
                                                                    if (Files.exists(filePath)) {
                                                                        MessageManager.sendColored("Error: " + alias + ".json already exists.");
                                                                        return 1;
                                                                    }

                                                                    Explain explain = new Explain(alias, author, message);
                                                                    try {
                                                                        FileManager.createJsonFileInSubfolder("explain", alias, explain);
                                                                        MessageManager.sendColored("Created: " + alias + ".json");
                                                                    } catch (IOException e) {
                                                                        MessageManager.sendColored("Error creating file.");
                                                                        e.printStackTrace();
                                                                    }
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("explain")
                                .executes(ctx -> {
                                    Path folder = FabricLoader.getInstance()
                                            .getConfigDir()
                                            .resolve("loreutils")
                                            .resolve("explain");
                                    if (!Files.exists(folder)) {
                                        MessageManager.sendColored("No explain files found.");
                                        return 1;
                                    }
                                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
                                        StringBuilder sb = new StringBuilder("Available explains: ");
                                        for (Path path : stream) {
                                            sb.append(path.getFileName().toString().replace(".json", "")).append(", ");
                                        }
                                        String list = sb.toString();
                                        MessageManager.sendColored(list.endsWith(", ")
                                                ? list.substring(0, list.length() - 2)
                                                : list);
                                    } catch (IOException e) {
                                        MessageManager.sendColored("Error reading explain folder.");
                                    }
                                    return 1;
                                })
                                .then(argument("alias", word())
                                        .executes(ctx -> {
                                            String alias = ctx.getArgument("alias", String.class);
                                            Path file = FabricLoader.getInstance()
                                                    .getConfigDir()
                                                    .resolve("loreutils")
                                                    .resolve("explain")
                                                    .resolve(alias + ".json");
                                            if (!Files.exists(file)) {
                                                MessageManager.sendColored("Explain file \"" + alias + "\" not found.");
                                                return 1;
                                            }
                                            try (Reader reader = Files.newBufferedReader(file)) {
                                                Explain explain = new com.google.gson.Gson().fromJson(reader, Explain.class);
                                                MessageManager.sendColored(explain.message);
                                            } catch (IOException e) {
                                                MessageManager.sendColored("Error reading explain file.");
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }
}