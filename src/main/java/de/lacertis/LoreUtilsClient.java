package de.lacertis;

import com.google.gson.Gson;
import de.lacertis.loreutils.*;
import de.lacertis.loreutils.area.AreaChecker;
import de.lacertis.loreutils.command.ClientCommands;
import de.lacertis.loreutils.config.ModConfig;
import de.lacertis.loreutils.data.Pathway;
import de.lacertis.loreutils.pathway.PathwayConfig;
import de.lacertis.loreutils.solver.Ingenuity.CalibService;
import de.lacertis.loreutils.solver.Ingenuity.IngenuityInput;
import de.lacertis.loreutils.solver.Ingenuity.IngenuityPlanRunner;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Box;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class LoreUtilsClient implements ClientModInitializer {
    public static final IngenuityPlanRunner ingenuityPlanRunner = new IngenuityPlanRunner(new IngenuityInput());

    private static final Logger LOGGER = LogManager.getLogger("LoreUtils");
    private static final String BASE_DOMAIN = "pvplegacy.net";
    private boolean activeForServer = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ClientCommands.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String host = extractHost(handler.getConnection().getAddress());
            if (isWhitelisted(host) && !activeForServer) {
                try {
                    initForServer();
                    activeForServer = true;
                } catch (Exception e) {
                    LOGGER.error("Failed to initialize LoreUtils for server", e);
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (activeForServer) {
                uninitForServer();
                activeForServer = false;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (CalibService.get().isRunning()) {
                CalibService.get().tick();
            }
            if (ingenuityPlanRunner.isRunning()) {
                ingenuityPlanRunner.tick();
            }
        });

        // Attach Lectern "Copy" button via Fabric Screen Events
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            try {
                de.lacertis.loreutils.lectern.ui.LecternCopyButton.attachTo(screen);
            } catch (Throwable ignored) {
            }
        });
    }

    private String extractHost(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress) address).getHostString().toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private boolean isWhitelisted(String host) {
        return host.equals(BASE_DOMAIN) || host.endsWith("." + BASE_DOMAIN);
    }

    private void initForServer() {
        LOGGER.info("Initializing LoreUtils for server");

        PathwayConfig config = new PathwayConfig();
        loadPathwaysInto(config);

        AreaChecker.addArea(new Box(-11213, 35, 12646.5, -11261, 60, 12692), PlayerArea.LIGHTS_OUT);
        AreaChecker.addArea(new Box(-11100.5, 1, 12621.5, -11400.5, 70, 12100.5), PlayerArea.ANUAR_GEM);
        AreaChecker.addArea(new Box(-12127, 50, 12590, -12232, 80, 12570), PlayerArea.INGENUITY);

        EspRender.init();
        AreaChecker.init();
        LineRender.init();
    }

    private void uninitForServer() {
        LOGGER.info("Uninitializing LoreUtils");

        EspRender.uninit();
        LineRender.uninit();
        AreaChecker.uninit();
    }

    private void loadPathwaysInto(PathwayConfig target) {
        Path folder = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("loreutils")
                .resolve("pathways")
                .resolve("ingenuity");

        if (!Files.isDirectory(folder)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
            Gson gson = new Gson();
            for (Path file : stream) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    Pathway p = gson.fromJson(reader, Pathway.class);
                    if (p != null) {
                        target.add(p);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to load pathway from {}", file, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read pathways directory", e);
        }
    }

}