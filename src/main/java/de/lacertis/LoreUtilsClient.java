package de.lacertis;

import de.lacertis.client.AreaChecker;
import de.lacertis.client.EspRender;
import de.lacertis.client.PuzzleInput;
import de.lacertis.client.PuzzleSolver;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class LoreUtilsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Box testBox = new Box(0, 70, 0, 10, 100, 10);

        AreaChecker.addArea(testBox);
        EspRender.registerPosition(new BlockPos(100, 64, 100));

        AreaChecker.init();
        EspRender.init();

        boolean[][] lightStates = new boolean[][] {
                {true, false, true, false, true, false, true},
                {true, false, true, false, true, false, false},
                {true, false, false, true, true, true, true},
                {false, false, true, true, false, false, true},
                {true, false, true, false, true, true, true},
                {true, true, false, true, true, false, true},
                {true, true, true, true, true, false, true}
        };

        PuzzleSolver.Tile[][] grid = PuzzleInput.createGridFromLights(lightStates);

        System.out.println("Teste Lösung für alle Lichter AN:");
        System.out.println(PuzzleSolver.solveAllOnOptimized(grid));

        System.out.println("Teste Lösung für alle Lichter AUS:");
        System.out.println(PuzzleSolver.solveAllOffOptimized(grid));
    }
}