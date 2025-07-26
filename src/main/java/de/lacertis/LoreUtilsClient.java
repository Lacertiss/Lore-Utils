package de.lacertis;

import com.google.gson.Gson;
import de.lacertis.client.*;
import de.lacertis.client.area.AreaChecker;
import de.lacertis.client.command.ClientCommands;
import de.lacertis.client.config.LoreModConfig;
import de.lacertis.client.data.Pathway;
import de.lacertis.client.pathway.PathwayConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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

    private static final Logger LOGGER = LogManager.getLogger("LoreUtils");
    private static final String BASE_DOMAIN = "pvplegacy.net";
    private boolean activeForServer = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(LoreModConfig.class, Toml4jConfigSerializer::new);
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
                .resolve("pathways");

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