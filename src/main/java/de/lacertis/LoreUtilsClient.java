package de.lacertis;

import de.lacertis.client.AreaChecker;
import de.lacertis.client.EspRender;
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
    }
}