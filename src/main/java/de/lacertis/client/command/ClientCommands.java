package de.lacertis.client.command;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.lacertis.client.EspRender;
import de.lacertis.client.LineRender;
import de.lacertis.client.MessageManager;
import de.lacertis.client.PlayerArea;
import de.lacertis.client.config.ConfigTranslator;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.data.Explain;
import de.lacertis.client.data.FileManager;
import de.lacertis.client.data.Pathway;
import de.lacertis.client.pathway.PathwayBuilder;
import de.lacertis.client.pathway.PathwayElement;
import de.lacertis.client.pathway.PathwayType;
import de.lacertis.client.solver.LightsOutInput;
import de.lacertis.client.solver.LightsOutSolver;
import de.lacertis.client.solver.RenderSolvedLightsOut;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.Reader;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ClientCommands {

    private static PathwayBuilder currentBuilder;

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
                                .then(literal("pathway")
                                        .then(argument("id", word())
                                                .executes(ctx -> {
                                                    String id = StringArgumentType.getString(ctx, "id");
                                                    currentBuilder = new PathwayBuilder().setId(id);
                                                    MessageManager.sendColored("Pathway mode started for " + id + ".");
                                                    return 1;
                                                })
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
                        .then(literal("path")
                                .then(literal("add")
                                        .then(literal("line")
                                                .executes(ctx -> {
                                                    BlockPos bs = MinecraftClient.getInstance().player.getBlockPos();
                                                    BlockPos be = bs;
                                                    currentBuilder.addElement(new PathwayElement(PathwayType.LINE, bs, be));
                                                    MessageManager.sendColored("Line added from " + bs + " to " + be + ".");
                                                    return 1;
                                                })
                                                .then(argument("start", Vec3ArgumentType.vec3())
                                                        .then(argument("end", Vec3ArgumentType.vec3())
                                                                .executes(ctx -> {
                                                                    Vec3d vs = ctx.getArgument("start", Vec3d.class);
                                                                    Vec3d ve = ctx.getArgument("end", Vec3d.class);
                                                                    BlockPos bs = new BlockPos((int) vs.x, (int) vs.y, (int) vs.z);
                                                                    BlockPos be = new BlockPos((int) ve.x, (int) ve.y, (int) ve.z);
                                                                    currentBuilder.addElement(new PathwayElement(PathwayType.LINE, bs, be));
                                                                    MessageManager.sendColored("Line added from " + bs + " to " + be + ".");
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                        .then(literal("block")
                                                .executes(ctx -> {
                                                    BlockPos bp = MinecraftClient.getInstance().player.getBlockPos();
                                                    currentBuilder.addElement(new PathwayElement(PathwayType.BLOCK_HIGHLIGHT, bp, null));
                                                    MessageManager.sendColored("Block highlight added at " + bp + ".");
                                                    return 1;
                                                })
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(ctx -> {
                                                            Vec3d v = ctx.getArgument("pos", Vec3d.class);
                                                            BlockPos bp = new BlockPos((int) v.x, (int) v.y, (int) v.z);
                                                            currentBuilder.addElement(new PathwayElement(PathwayType.BLOCK_HIGHLIGHT, bp, null));
                                                            MessageManager.sendColored("Block highlight added at " + bp + ".");
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(literal("enable")
                                        .then(argument("id", word())
                                                .executes(ctx -> {
                                                    String id = StringArgumentType.getString(ctx, "id");
                                                    Path folder = FabricLoader.getInstance()
                                                            .getConfigDir()
                                                            .resolve("loreutils")
                                                            .resolve("pathways");
                                                    Path file = folder.resolve(id + ".json");
                                                    if (!Files.exists(file)) {
                                                        MessageManager.sendColored("Pathway " + id + " not found.");
                                                        return 1;
                                                    }
                                                    try {
                                                        Pathway p = new Gson().fromJson(Files.newBufferedReader(file), Pathway.class);
                                                        p.setEnabled(true);
                                                        Files.createDirectories(folder);
                                                        try (Writer w = Files.newBufferedWriter(file)) {
                                                            new GsonBuilder().setPrettyPrinting().create().toJson(p, w);
                                                        }
                                                        MessageManager.sendColored("Pathway " + id + " enabled.");
                                                    } catch (IOException e) {
                                                        MessageManager.sendColored("Error enabling pathway " + id + ".");
                                                        e.printStackTrace();
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                .then(literal("disable")
                                        .then(argument("id", word())
                                                .executes(ctx -> {
                                                    String id = StringArgumentType.getString(ctx, "id");
                                                    Path folder = FabricLoader.getInstance()
                                                            .getConfigDir()
                                                            .resolve("loreutils")
                                                            .resolve("pathways");
                                                    Path file = folder.resolve(id + ".json");
                                                    if (!Files.exists(file)) {
                                                        MessageManager.sendColored("Pathway " + id + " not found.");
                                                        return 1;
                                                    }
                                                    try {
                                                        Pathway p = new Gson().fromJson(Files.newBufferedReader(file), Pathway.class);
                                                        p.setEnabled(false);
                                                        Files.createDirectories(folder);
                                                        try (Writer w = Files.newBufferedWriter(file)) {
                                                            new GsonBuilder().setPrettyPrinting().create().toJson(p, w);
                                                        }
                                                        MessageManager.sendColored("Pathway " + id + " disabled.");
                                                    } catch (IOException e) {
                                                        MessageManager.sendColored("Error disabling pathway " + id + ".");
                                                        e.printStackTrace();
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                .then(literal("finish")
                                        .executes(ctx -> {
                                            currentBuilder.build().ifPresentOrElse(p -> {
                                                try {
                                                    FileManager.createJsonFileInSubfolder("pathways", p.getId(), p);
                                                    MessageManager.sendColored("Pathway " + p.getId() + " created.");
                                                } catch (IOException e) {
                                                    MessageManager.sendColored("Error creating pathway " + p.getId() + ".");
                                                    e.printStackTrace();
                                                }
                                            }, () -> MessageManager.sendColored("No pathway to finish."));
                                            currentBuilder = null;
                                            return 1;
                                        })
                                )
                                .then(literal("cancel")
                                        .executes(ctx -> {
                                            currentBuilder = null;
                                            MessageManager.sendColored("Cancelled");
                                            return 1;
                                        })
                                )
                                .then(literal("list")
                                        .executes(ctx -> {
                                            Path folder = FabricLoader.getInstance()
                                                    .getConfigDir()
                                                    .resolve("loreutils")
                                                    .resolve("pathways");
                                            StringBuilder sb = new StringBuilder("Pathways: ");
                                            try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.json")) {
                                                for (Path f : ds) {
                                                    Pathway p = new Gson().fromJson(Files.newBufferedReader(f), Pathway.class);
                                                    sb.append(p.getId())
                                                            .append(p.isEnabled() ? " (on), " : " (off), ");
                                                }
                                            } catch (IOException e) {
                                                MessageManager.sendColored("Error reading pathways list.");
                                                e.printStackTrace();
                                                return 1;
                                            }
                                            MessageManager.sendColored(sb.toString().replaceAll(", $", ""));
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("pathway")
                                .then(argument("id", word())
                                        .executes(ctx -> {
                                            String id = StringArgumentType.getString(ctx, "id");
                                            Path folder = FabricLoader.getInstance()
                                                    .getConfigDir()
                                                    .resolve("loreutils")
                                                    .resolve("pathways");
                                            Path file = folder.resolve(id + ".json");
                                            if (!Files.exists(file)) {
                                                MessageManager.sendColored("Pathway " + id + " not found.");
                                                return 1;
                                            }
                                            try (Reader reader = Files.newBufferedReader(file)) {
                                                Pathway p = new Gson().fromJson(reader, Pathway.class);
                                                LineRender.unregisterAllLines();
                                                EspRender.unregisterAllPositions();
                                                if (p.isEnabled()) {
                                                    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                                                    float[] rgb = ConfigTranslator.translate(config.primaryColor);
                                                    int r = (int)(rgb[0] * 255);
                                                    int g = (int)(rgb[1] * 255);
                                                    int b = (int)(rgb[2] * 255);
                                                    int a = (int)(ConfigTranslator.translateAlpha(config.alphaPercentage) * 255);

                                                    for (PathwayElement e : p.getElements()) {
                                                        if (e.getType() == PathwayType.LINE) {
                                                            Vec3d start = e.getPos1().toCenterPos();
                                                            Vec3d end = e.getPos2().toCenterPos();
                                                            LineRender.registerLine(start, end, r, g, b, a);
                                                        } else {
                                                            EspRender.registerPosition(e.getPos1());
                                                        }
                                                    }
                                                    MessageManager.sendColored("Pathway " + id + " rendered.");
                                                } else {
                                                    MessageManager.sendColored("Pathway " + id + " is disabled.");
                                                }
                                            } catch (IOException e) {
                                                MessageManager.sendColored("Error loading pathway " + id + ".");
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        })
                                )
                        )

        );
    }
}