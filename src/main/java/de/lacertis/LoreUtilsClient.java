package de.lacertis;

import de.lacertis.client.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class LoreUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Box testBox = new Box(0, 70, 0, 10, 100, 10);;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = handler.getConnection().getAddress().toString();
            if (serverAddress.contains("pvplegacy.net")) {
                System.out.println("Detected connection to PvP Legacy server.");
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