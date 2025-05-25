package de.lacertis;

import com.google.gson.Gson;
import de.lacertis.client.*;
import de.lacertis.client.area.AreaChecker;
import de.lacertis.client.command.ClientCommands;
import de.lacertis.client.config.ModConfig;
import de.lacertis.client.data.Pathway;
import de.lacertis.client.pathway.PathwayConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Box;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoreUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = handler.getConnection().getAddress().toString();
            if (serverAddress.contains("pvplegacy.net")) {
                init();
            } else {
                uninit();
            }
        });

    }

    private void init() {
        PathwayConfig config = new PathwayConfig();
        Path folder = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("loreutils")
                .resolve("pathways");

        if (Files.isDirectory(folder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.json")) {
                Gson gson = new Gson();
                for (Path file : stream) {
                    try (Reader reader = Files.newBufferedReader(file)) {
                        Pathway p = gson.fromJson(reader, Pathway.class);
                        config.add(p);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        AreaChecker.addArea(new Box(-11213, 35, 12646.5, -11261, 60, 12692), PlayerArea.LIGHTS_OUT);
        AreaChecker.addArea(new Box(-11100.5, 1, 12621.5, -11400.5, 70, 12100.5), PlayerArea.ANUAR_GEM);

        ClientCommands.register();

        EspRender.init();
        AreaChecker.init();

        LineRender lineRender = new LineRender();
        lineRender.init();
    }

    private void uninit() {
        EspRender.uninit();
        AreaChecker.uninit();
    }

}