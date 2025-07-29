package de.lacertis.loreutils.solver.Ingenuity;

import com.google.common.reflect.TypeToken;
import de.lacertis.loreutils.MessageManager;
import de.lacertis.loreutils.data.FileManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class PatternGoalIO {
    private static final String SUBFOLDER = "ingenuity";
    private static final String FILENAME = "goal_pattern.json";

    public static PatternGoal loadOrCreateDefault() {
        Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
        Map<String, List<String>> data;
        try {
            data = FileManager.loadJsonInSubfolder(SUBFOLDER, FILENAME, mapType);
        } catch (IOException e) {
            data = null;
        }

        if (data == null || data.isEmpty()) {
            Map<String, List<String>> defaults = new LinkedHashMap<>();
            defaults.put("WESTNORTH", Arrays.asList("EASTNORTH", "EASTWEST"));
            defaults.put("EASTNORTH", Arrays.asList("WESTNORTH", "EASTWEST"));
            defaults.put("WESTWEST", Arrays.asList("NORTHSOUTH", "WESTNORTH"));
            defaults.put("WESTEAST", Collections.singletonList("EASTSOUTH"));
            defaults.put("EASTWEST", Collections.singletonList("WESTSOUTH"));
            defaults.put("EASTEAST", Arrays.asList("EASTNORTH", "NORTHSOUTH"));
            defaults.put("WESTSOUTH", Arrays.asList("EASTWEST", "WESTSOUTH"));
            defaults.put("EASTSOUTH", Arrays.asList("EASTWEST", "EASTSOUTH"));
            try {
                FileManager.saveJsonInSubfolder(SUBFOLDER, FILENAME, defaults);
                MessageManager.sendChatColored("&aIngenuity: Created default goal_pattern.json.");
            } catch (IOException ignored) {}
            data = defaults;
        }

        Map<Slot, EnumSet<Tile>> allowed = new EnumMap<>(Slot.class);
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            Slot slot;
            try {
                slot = Slot.valueOf(entry.getKey());
            } catch (IllegalArgumentException ex) {
                continue;
            }
            EnumSet<Tile> tiles = EnumSet.noneOf(Tile.class);
            for (String tileName : entry.getValue()) {
                try {
                    tiles.add(Tile.valueOf(tileName));
                } catch (IllegalArgumentException ignored) {}
            }
            if (!tiles.isEmpty()) allowed.put(slot, tiles);
        }

        if (allowed.isEmpty()) {
            Map<String, List<String>> defaults = new LinkedHashMap<>();
            defaults.put("WESTNORTH", Arrays.asList("EASTNORTH", "EASTWEST"));
            defaults.put("EASTNORTH", Arrays.asList("WESTNORTH", "EASTWEST"));
            defaults.put("WESTWEST", Arrays.asList("NORTHSOUTH", "WESTNORTH"));
            defaults.put("WESTEAST", Collections.singletonList("EASTSOUTH"));
            defaults.put("EASTWEST", Collections.singletonList("WESTSOUTH"));
            defaults.put("EASTEAST", Arrays.asList("EASTNORTH", "NORTHSOUTH"));
            defaults.put("WESTSOUTH", Arrays.asList("EASTWEST", "WESTSOUTH"));
            defaults.put("EASTSOUTH", Arrays.asList("EASTWEST", "EASTSOUTH"));
            try {
                FileManager.saveJsonInSubfolder(SUBFOLDER, FILENAME, defaults);
            } catch (IOException ignored) {}
            for (Map.Entry<String, List<String>> entry : defaults.entrySet()) {
                Slot slot;
                try {
                    slot = Slot.valueOf(entry.getKey());
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                EnumSet<Tile> tiles = EnumSet.noneOf(Tile.class);
                for (String tileName : entry.getValue()) {
                    try {
                        tiles.add(Tile.valueOf(tileName));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!tiles.isEmpty()) allowed.put(slot, tiles);
            }
            MessageManager.sendChatColored("&eIngenuity: goal_pattern.json was invalid – restored defaults.");
        }

        return allowed.isEmpty() ? null : new PatternGoal(allowed);
    }

    public static PatternGoal loadOrNull() {
        return loadOrCreateDefault();
    }
}
