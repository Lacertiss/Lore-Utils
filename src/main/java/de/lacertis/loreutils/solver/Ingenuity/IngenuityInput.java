package de.lacertis.loreutils.solver.Ingenuity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;


public class IngenuityInput {
    private final Map<Slot, IngenuityCoords> slotToCoords;

    public IngenuityInput() {
        slotToCoords = new EnumMap<>(Slot.class);
        slotToCoords.put(Slot.EASTEAST, IngenuityCoords.EASTEAST);
        slotToCoords.put(Slot.EASTNORTH, IngenuityCoords.EASTNORTH);
        slotToCoords.put(Slot.EASTWEST, IngenuityCoords.EASTWEST);
        slotToCoords.put(Slot.EASTSOUTH, IngenuityCoords.EASTSOUTH);
        slotToCoords.put(Slot.WESTWEST, IngenuityCoords.WESTWEST);
        slotToCoords.put(Slot.WESTNORTH, IngenuityCoords.WESTNORTH);
        slotToCoords.put(Slot.WESTEAST, IngenuityCoords.WESTEAST);
        slotToCoords.put(Slot.WESTSOUTH, IngenuityCoords.WESTSOUTH);
    }

    public Tile readTile(Slot slot) {
        World world = requireWorld();
        BlockPos junction = toBlockPos(slotToCoords.get(slot));
        BlockPos[] neighbors = neighbors(junction);

        boolean n = isLodestone(world, neighbors[0]);
        boolean e = isLodestone(world, neighbors[1]);
        boolean s = isLodestone(world, neighbors[2]);
        boolean w = isLodestone(world, neighbors[3]);

        return mapTile(n, e, s, w, slot, junction);
    }

    public Tile[] readSnapshotOnce() {
        Slot[] slots = Slot.values();
        Tile[] snapshot = new Tile[slots.length];

        for (int i = 0; i < slots.length; i++) {
            snapshot[i] = readTile(slots[i]);
        }

        return snapshot;
    }

    public Tile[] readStableSnapshot(int consecutiveEquals, long timeoutMs, long pollIntervalMs) {
        long startTime = System.currentTimeMillis();
        Tile[] lastSnapshot = null;
        int consecutiveCount = 0;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Tile[] currentSnapshot = readSnapshotOnce();

            if (lastSnapshot != null && Arrays.equals(currentSnapshot, lastSnapshot)) {
                consecutiveCount++;
                if (consecutiveCount >= consecutiveEquals) {
                    return currentSnapshot;
                }
            } else {
                consecutiveCount = 1;
            }

            lastSnapshot = currentSnapshot;

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
            }
        }

        throw new RuntimeException("TIMEOUT_UNSTABLE");
    }

    private World requireWorld() {
        World world = MinecraftClient.getInstance().world;
        if (world == null) {
            throw new IllegalStateException("WORLD_NULL");
        }
        return world;
    }

    private BlockPos toBlockPos(IngenuityCoords coords) {
        return new BlockPos((int) coords.getX(), (int) coords.getY(), (int) coords.getZ());
    }

    private BlockPos[] neighbors(BlockPos junction) {
        return new BlockPos[] {
            junction.add(0, 0, -1),
            junction.add(1, 0, 0),
            junction.add(0, 0, 1),
            junction.add(-1, 0, 0)
        };
    }

    private boolean isLodestone(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.LODESTONE);
    }

    private Tile mapTile(boolean n, boolean e, boolean s, boolean w, Slot slot, BlockPos junction) {
        if (e && w && !n && !s) {
            return Tile.EASTWEST;
        }
        if (n && s && !e && !w) {
            return Tile.NORTHSOUTH;
        }
        if (e && n && !s && !w) {
            return Tile.EASTNORTH;
        }
        if (e && s && !n && !w) {
            return Tile.EASTSOUTH;
        }
        if (w && s && !n && !e) {
            return Tile.WESTSOUTH;
        }
        if (w && n && !s && !e) {
            return Tile.WESTNORTH;
        }

        throw new RuntimeException(String.format("UNMAPPABLE_TILE at SLOT=%s pos=(%d,%d,%d), flags n=%s e=%s s=%s w=%s", slot.name(), junction.getX(), junction.getY(), junction.getZ(), n, e, s, w));
    }
}
