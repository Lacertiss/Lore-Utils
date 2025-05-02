package de.lacertis.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class AreaChecker {
    private static final List<Box> AREAS = new ArrayList<>();
    private static boolean initialized = false;

    public static void addArea(Box area) {
        AREAS.add(area);
    }

    public static void init() {
        if (initialized) return;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                Vec3d playerPos = client.player.getPos();
                for (Box area : AREAS) {
                    if (area.contains(playerPos)) {
                        System.out.println("Player is in area: " + area);
                    }
                }
            }
        });
        initialized = true;
    }

    public static void uninit() {
        AREAS.clear();
        initialized = false;
    }
}