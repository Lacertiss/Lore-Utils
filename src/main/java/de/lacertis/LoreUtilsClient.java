package de.lacertis;

import de.lacertis.client.*;
import de.lacertis.client.area.AreaChecker;
import de.lacertis.client.command.ClientCommands;
import de.lacertis.client.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.math.Box;

public class LoreUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = handler.getConnection().getAddress().toString();
            if (serverAddress.contains("pvplegacy.net")) {

                AreaChecker.addArea(new Box(-11213, 35, 12646.5, -11261, 60, 12692), PlayerArea.LIGHTS_OUT);
                AreaChecker.addArea(new Box(-11100.5, 1, 12621.5, -11400.5, 70, 12100.5), PlayerArea.ANUAR_GEM);

                ClientCommands.register();

                EspRender.init();
                AreaChecker.init();

                LineRender lineRender = new LineRender();
                lineRender.init();
            } else {
                EspRender.uninit();
                AreaChecker.uninit();
            }
        });

    }
}