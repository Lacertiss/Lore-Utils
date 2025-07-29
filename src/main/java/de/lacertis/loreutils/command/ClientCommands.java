package de.lacertis.loreutils.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.lacertis.LoreUtilsClient;
import de.lacertis.loreutils.EspRender;
import de.lacertis.loreutils.LineRender;
import de.lacertis.loreutils.MessageManager;
import de.lacertis.loreutils.config.ConfigTranslator;
import de.lacertis.loreutils.config.ModConfig;
import de.lacertis.loreutils.data.Explain;
import de.lacertis.loreutils.data.FileManager;
import de.lacertis.loreutils.data.Pathway;
import de.lacertis.loreutils.pathway.PathwayBuilder;
import de.lacertis.loreutils.pathway.PathwayElement;
import de.lacertis.loreutils.pathway.PathwayType;
import de.lacertis.loreutils.solver.Ingenuity.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommands {

    private static PathwayBuilder currentBuilder;
    private static BlockPos tempLineStart;

    private static List<String> listExplainAliases() {
        List<String> aliases = new ArrayList<>();
        Path folder = FabricLoader.getInstance().getConfigDir().resolve("loreutils").resolve("explain");
        if (Files.exists(folder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
                for (Path path : stream) {
                    String filename = path.getFileName().toString();
                    if (filename.endsWith(".json")) {
                        aliases.add(filename.replace(".json", ""));
                    }
                }
            } catch (IOException ignored) {}
        }
        return aliases;
    }

    private static List<String> listPathwayIds() {
        List<String> ids = new ArrayList<>();
        Path folder = FabricLoader.getInstance().getConfigDir().resolve("loreutils").resolve("pathways");
        if (Files.exists(folder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
                for (Path path : stream) {
                    String filename = path.getFileName().toString();
                    if (filename.endsWith(".json")) {
                        ids.add(filename.replace(".json", ""));
                    }
                }
            } catch (IOException ignored) {}
        }
        return ids;
    }

    private static final SuggestionProvider<FabricClientCommandSource> EXPLAIN_ALIAS_PROVIDER = (context, builder) -> {
        for (String alias : listExplainAliases()) {
            builder.suggest(alias);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> PATHWAY_ID_PROVIDER = (context, builder) -> {
        for (String id : listPathwayIds()) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    };

    private static int ok(String fmt, Object... args) {
        MessageManager.sendChatColored("&a✔ " + String.format(fmt, args));
        return Command.SINGLE_SUCCESS;
    }

    private static int err(String fmt, Object... args) {
        MessageManager.sendChatColored("&c✖ " + String.format(fmt, args));
        return Command.SINGLE_SUCCESS;
    }

    private static int info(String fmt, Object... args) {
        MessageManager.sendChatColored("&7" + String.format(fmt, args));
        return Command.SINGLE_SUCCESS;
    }

    private static boolean requireBuilder() {
        if (currentBuilder == null) {
            err("No pathway builder active. Use /lore pathway builder start <id>");
            return false;
        }
        return true;
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        var explainListCmd = literal("list")
                .executes(ctx -> {
                    List<String> aliases = listExplainAliases();
                    if (aliases.isEmpty()) {
                        return err("No explain files found.");
                    }
                    return info("Available explains: %s", String.join(", ", aliases));
                });

        var explainShowCmd = literal("show")
                .then(argument("alias", word())
                        .suggests(EXPLAIN_ALIAS_PROVIDER)
                        .executes(ctx -> {
                            String alias = ctx.getArgument("alias", String.class);
                            Path file = FabricLoader.getInstance()
                                    .getConfigDir()
                                    .resolve("loreutils")
                                    .resolve("explain")
                                    .resolve(alias + ".json");
                            if (!Files.exists(file)) {
                                return err("Explain file \"%s\" not found.", alias);
                            }
                            try (Reader reader = Files.newBufferedReader(file)) {
                                Explain explain = new Gson().fromJson(reader, Explain.class);
                                return info(explain.message);
                            } catch (IOException e) {
                                return err("Error reading explain file.");
                            }
                        }));

        var explainCreateCmd = literal("create")
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
                                                return err("Error: %s.json already exists.", alias);
                                            }

                                            Explain explain = new Explain(alias, author, message);
                                            try {
                                                FileManager.createJsonFileInSubfolder("explain", alias, explain);
                                                return ok("Created: %s.json", alias);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return err("Error creating file.");
                                            }
                                       }))));

        var explainRoot = literal("explain")
                .then(explainListCmd)
                .then(explainShowCmd)
                .then(explainCreateCmd);

        var builderStartCmd = literal("start")
                .then(argument("id", word())
                        .executes(ctx -> {
                            String id = ctx.getArgument("id", String.class);
                            currentBuilder = new PathwayBuilder().setId(id);
                            tempLineStart = null;
                            return ok("Pathway builder mode started for %s.", id);
                        }));

        var builderAddLineCmd = literal("add")
                .then(literal("line")
                        .executes(ctx -> {
                            if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                            BlockPos pos = MinecraftClient.getInstance().player.getBlockPos();
                            if (tempLineStart == null) {
                                tempLineStart = pos;
                                return info("Start position set to %s.", pos.toShortString());
                            } else {
                                currentBuilder.addElement(new PathwayElement(PathwayType.LINE, tempLineStart, pos));
                                info("Line added from %s to %s.", tempLineStart.toShortString(), pos.toShortString());
                                tempLineStart = null;
                                return Command.SINGLE_SUCCESS;
                            }
                        })
                        .then(literal("here")
                                .executes(ctx -> {
                                    if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                                    BlockPos pos = MinecraftClient.getInstance().player.getBlockPos();
                                    if (tempLineStart == null) {
                                        tempLineStart = pos;
                                        return info("Start position set to %s.", pos.toShortString());
                                    } else {
                                        currentBuilder.addElement(new PathwayElement(PathwayType.LINE, tempLineStart, pos));
                                        info("Line added from %s to %s.", tempLineStart.toShortString(), pos.toShortString());
                                        tempLineStart = null;
                                        return Command.SINGLE_SUCCESS;
                                    }
                                }))
                        .then(argument("sx", DoubleArgumentType.doubleArg())
                                .then(argument("sy", DoubleArgumentType.doubleArg())
                                        .then(argument("sz", DoubleArgumentType.doubleArg())
                                                .then(argument("ex", DoubleArgumentType.doubleArg())
                                                        .then(argument("ey", DoubleArgumentType.doubleArg())
                                                                .then(argument("ez", DoubleArgumentType.doubleArg())
                                                                        .executes(ctx -> {
                                                                            if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                                                                            BlockPos start = new BlockPos(
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "sx"),
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "sy"),
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "sz"));
                                                                            BlockPos end = new BlockPos(
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "ex"),
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "ey"),
                                                                                    (int) DoubleArgumentType.getDouble(ctx, "ez"));
                                                                            currentBuilder.addElement(new PathwayElement(PathwayType.LINE, start, end));
                                                                            return ok("Line added from %s to %s.", start.toShortString(), end.toShortString());
                                                                        }))))))));

        var builderAddBlockCmd = literal("add")
                .then(literal("block")
                        .executes(ctx -> {
                            if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                            BlockPos bp = MinecraftClient.getInstance().player.getBlockPos();
                            currentBuilder.addElement(new PathwayElement(PathwayType.BLOCK_HIGHLIGHT, bp, null));
                            return ok("Block highlight added at %s.", bp.toShortString());
                        })
                        .then(literal("here")
                                .executes(ctx -> {
                                    if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                                    BlockPos bp = MinecraftClient.getInstance().player.getBlockPos();
                                    currentBuilder.addElement(new PathwayElement(PathwayType.BLOCK_HIGHLIGHT, bp, null));
                                    return ok("Block highlight added at %s.", bp.toShortString());
                                }))
                        .then(argument("x", DoubleArgumentType.doubleArg())
                                .then(argument("y", DoubleArgumentType.doubleArg())
                                        .then(argument("z", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                                                    BlockPos bp = new BlockPos(
                                                            (int) DoubleArgumentType.getDouble(ctx, "x"),
                                                            (int) DoubleArgumentType.getDouble(ctx, "y"),
                                                            (int) DoubleArgumentType.getDouble(ctx, "z"));
                                                    currentBuilder.addElement(new PathwayElement(PathwayType.BLOCK_HIGHLIGHT, bp, null));
                                                    return ok("Block highlight added at %s.", bp.toShortString());
                                                })))));

        var builderFinishCmd = literal("finish")
                .executes(ctx -> {
                    if (!requireBuilder()) return Command.SINGLE_SUCCESS;
                    currentBuilder.build().ifPresentOrElse(p -> {
                        try {
                            FileManager.createJsonFileInSubfolder("pathways", p.getId(), p);
                            ok("Pathway %s created.", p.getId());
                        } catch (IOException e) {
                            err("Error creating pathway %s.", p.getId());
                            e.printStackTrace();
                        }
                    }, () -> err("No pathway to finish."));
                    currentBuilder = null;
                    tempLineStart = null;
                    return Command.SINGLE_SUCCESS;
                });

        var builderCancelCmd = literal("cancel")
                .executes(ctx -> {
                    if (currentBuilder == null) {
                        return err("No pathway builder in progress.");
                    }
                    currentBuilder = null;
                    tempLineStart = null;
                    return info("Cancelled");
                });

        var pathwayBuilderRoot = literal("builder")
                .then(builderStartCmd)
                .then(builderAddLineCmd)
                .then(builderAddBlockCmd)
                .then(builderFinishCmd)
                .then(builderCancelCmd);

        var pathwayListCmd = literal("list")
                .executes(ctx -> {
                    Path folder = FabricLoader.getInstance()
                            .getConfigDir()
                            .resolve("loreutils")
                            .resolve("pathways");
                    if (!Files.exists(folder)) {
                        return info("Pathways: (none)");
                    }
                    StringBuilder sb = new StringBuilder("Pathways: ");
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.json")) {
                        for (Path f : ds) {
                            Pathway p = new Gson().fromJson(Files.newBufferedReader(f), Pathway.class);
                            sb.append(p.getId())
                                    .append(p.isEnabled() ? " &7(&aon&7), " : " &7(&coff&7), ");
                        }
                        String result = sb.toString().replaceAll(", $", "");
                        return info(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return err("Error reading pathways list.");
                    }
                });

        var pathwayRenderCmd = literal("render")
                .then(argument("id", word())
                        .suggests(PATHWAY_ID_PROVIDER)
                        .executes(ctx -> {
                            String id = ctx.getArgument("id", String.class);
                            Path file = FabricLoader.getInstance()
                                    .getConfigDir()
                                    .resolve("loreutils")
                                    .resolve("pathways")
                                    .resolve(id + ".json");
                            if (!Files.exists(file)) {
                                return err("Pathway \"%s\" not found.", id);
                            }
                            try (Reader reader = Files.newBufferedReader(file)) {
                                Pathway pathway = new Gson().fromJson(reader, Pathway.class);

                                LineRender.unregisterAllLines();
                                EspRender.unregisterAllPositions();

                                ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                                int color = config.primaryColor;
                                float[] rgb = ConfigTranslator.translate(color);
                                float alpha = ConfigTranslator.translateAlpha(config.alphaPercentage);

                                for (PathwayElement element : pathway.getElements()) {
                                    switch (element.getType()) {
                                        case LINE -> {
                                            Vec3d start = Vec3d.ofCenter(element.getPos1());
                                            Vec3d end = Vec3d.ofCenter(element.getPos2());
                                            LineRender.registerLine(start, end,
                                                (int)(rgb[0] * 255),
                                                (int)(rgb[1] * 255),
                                                (int)(rgb[2] * 255),
                                                (int)(alpha * 255));
                                        }
                                        case BLOCK_HIGHLIGHT -> EspRender.registerPosition(element.getPos1());
                                    }
                                }
                                return ok("Rendered pathway %s", id);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return err("Error reading pathway file");
                            }
                        }));

        var pathwayEnableCmd = literal("enable")
                .then(argument("id", word())
                        .suggests(PATHWAY_ID_PROVIDER)
                        .executes(ctx -> {
                            String id = ctx.getArgument("id", String.class);
                            Path file = FabricLoader.getInstance()
                                    .getConfigDir()
                                    .resolve("loreutils")
                                    .resolve("pathways")
                                    .resolve(id + ".json");
                            if (!Files.exists(file)) {
                                return err("Pathway \"%s\" not found.", id);
                            }
                            try (Reader reader = Files.newBufferedReader(file)) {
                                Pathway pathway = new Gson().fromJson(reader, Pathway.class);
                                pathway.setEnabled(true);
                                try (Writer writer = Files.newBufferedWriter(file)) {
                                    new GsonBuilder().setPrettyPrinting().create().toJson(pathway, writer);
                                }
                                return ok("Enabled pathway %s", id);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return err("Error updating pathway");
                            }
                        }));

        var pathwayDisableCmd = literal("disable")
                .then(argument("id", word())
                        .suggests(PATHWAY_ID_PROVIDER)
                        .executes(ctx -> {
                            String id = ctx.getArgument("id", String.class);
                            Path file = FabricLoader.getInstance()
                                    .getConfigDir()
                                    .resolve("loreutils")
                                    .resolve("pathways")
                                    .resolve(id + ".json");
                            if (!Files.exists(file)) {
                                return err("Pathway \"%s\" not found.", id);
                            }
                            try (Reader reader = Files.newBufferedReader(file)) {
                                Pathway pathway = new Gson().fromJson(reader, Pathway.class);
                                pathway.setEnabled(false);
                                try (Writer writer = Files.newBufferedWriter(file)) {
                                    new GsonBuilder().setPrettyPrinting().create().toJson(pathway, writer);
                                }
                                return ok("Disabled pathway %s", id);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return err("Error updating pathway");
                            }
                        }));

        var pathwayRoot = literal("pathway")
                .then(pathwayBuilderRoot)
                .then(pathwayListCmd)
                .then(pathwayRenderCmd)
                .then(pathwayEnableCmd)
                .then(pathwayDisableCmd);

        var ingenuityCalibrateCmd = literal("calibrate")
                .executes(ctx -> {
                    boolean started = CalibService.get().start();
                    if (started) return ok("Ingenuity calibration started. Perform EAST/WEST/LECTERN changes.");
                    return info("Calibration already running.");
                });

        var ingenuityStopCmd = literal("stop")
                .executes(ctx -> {
                    if (CalibService.get().isRunning()) {
                        CalibService.get().stop();
                        return ok("Calibration stopped.");
                    }
                    return info("Calibration is not running.");
                });

        var ingenuityStatusCmd = literal("status")
                .executes(ctx -> {
                    String s = CalibService.get().status();
                    return info("%s", s);
                });

        var ingenuityResetCmd = literal("reset")
                .executes(ctx -> {
                    CalibService.get().markStale();
                    CalibService.get().resetSessions();
                    return ok("Marked permutations as stale and cleared sessions. Use /lore ingenuity calibrate to re-learn.");
                });

        var ingenuitySolveCmd = literal("solve")
                .executes(ctx -> {
                    IngenuityPerms d = PermutationsStorage.loadOrDefault();
                    if (!PermutationsStorage.permsReady(d)) {
                        MessageManager.sendChatColored("&cIngenuity: Perms not learned. Run &e/lore ingenuity calibrate&c.");
                        return Command.SINGLE_SUCCESS;
                    }

                    Goal goal = PatternGoalIO.loadOrCreateDefault();
                    if (goal == null) {
                        MessageManager.sendChatColored("&eIngenuity: No goal configured. Create &fconfig/loreutils/ingenuity/goal_pattern.json&e.");
                        return Command.SINGLE_SUCCESS;
                    }

                    Tile[] start = null;
                    try {
                        start = new IngenuityInput().readStableSnapshot(2, 1500, 50);
                    } catch (Exception e) {
                        MessageManager.sendChatColored("&cIngenuity: Could not read stable snapshot.");
                        return Command.SINGLE_SUCCESS;
                    }

                    if (goal.isSatisfied(start)) {
                        MessageManager.sendChatColored("&aIngenuity: Already solved — nothing to do.");
                        return Command.SINGLE_SUCCESS;
                    }

                    List<Move> path = new IngenuitySolver().solve(start, goal, d, 100_000);
                    if (path == null || path.isEmpty()) {
                        MessageManager.sendChatColored("&cIngenuity: No path found.");
                        return Command.SINGLE_SUCCESS;
                    }

                    List<PlanUtils.Run> runs = PlanUtils.compress(path);
                    MessageManager.sendChatColored("&bIngenuity: Plan &7(" + runs.size() + " steps)&b ready. Starting preview…");

                    if (LoreUtilsClient.ingenuityPlanRunner.isRunning()) {
                        LoreUtilsClient.ingenuityPlanRunner.stop();
                    }

                    LoreUtilsClient.ingenuityPlanRunner.loadPlan(path);
                    LoreUtilsClient.ingenuityPlanRunner.start();
                    return Command.SINGLE_SUCCESS;
                });

        var ingenuityRoot = literal("ingenuity")
                .then(ingenuityCalibrateCmd)
                .then(ingenuityStopCmd)
                .then(ingenuityStatusCmd)
                .then(ingenuityResetCmd)
                .then(ingenuitySolveCmd);

        dispatcher.register(
                literal("lore")
                        .executes(ctx -> {
                            info("&bLoreUtils Commands:");
                            info("&7/lore explain list|show|create");
                            info("&7/lore pathway builder start|add|finish|cancel");
                            info("&7/lore pathway list|render|enable|disable");
                            info("&7/lore ingenuity calibrate|stop|status|reset|solve");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(explainRoot)
                        .then(pathwayRoot)
                        .then(ingenuityRoot)
        );
    }
}

