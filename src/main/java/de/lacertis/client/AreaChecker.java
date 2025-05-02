package de.lacertis.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class AreaChecker {
    private static final List<AreaEntry> AREAS = new ArrayList<>();
    private static boolean initialized = false;

    public static void addArea(Box area, PlayerArea areaType) {
        AREAS.add(new AreaEntry(area, areaType));
    }

    private static class AreaEntry {
        Box area;
        PlayerArea areaType;

        AreaEntry(Box area, PlayerArea areaType) {
            this.area = area;
            this.areaType = areaType;
        }
    }

    public static void init() {
        if (initialized)
            return;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                Vec3d playerPos = client.player.getPos();
                for (AreaEntry entry : AREAS) {
                    boolean contains = entry.area.contains(playerPos);
                    entry.areaType.setActive(contains);
                    if (contains) {
                        System.out.println("Player ist in Area: " + entry.area);
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