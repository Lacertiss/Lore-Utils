package de.lacertis;

import de.lacertis.client.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.math.Box;

public class LoreUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = handler.getConnection().getAddress().toString();
            if (serverAddress.contains("pvplegacy.net")) {
                System.out.println("Detected connection to PvP Legacy server.");

                AreaChecker.addArea(new Box(-11213, 35, 12646.5, -11261, 60, 12692), PlayerArea.LIGHTS_OUT);

                EspRender.init();
                AreaChecker.init();
            } else {
                EspRender.uninit();
                AreaChecker.init();
            }
        });
        LineRender lineRender = new LineRender();
        lineRender.init();

        //AreaChecker.addArea(testBox);
        //EspRender.registerPosition(new BlockPos(100, 64, 100));

        /*
        boolean[][] lightStates = new boolean[][] {
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false}
        };

        PuzzleSolver.Tile[][] grid = PuzzleInput.createGridFromLights(lightStates);

        System.out.println("Lights On:");
        System.out.println(PuzzleSolver.solveAllOnOptimized(grid));

        System.out.println("Lights Out:");
        System.out.println(PuzzleSolver.solveAllOffOptimized(grid));
        */
    }
}