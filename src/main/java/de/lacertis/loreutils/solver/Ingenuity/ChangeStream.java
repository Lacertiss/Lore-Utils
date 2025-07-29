package de.lacertis.loreutils.solver.Ingenuity;

import java.util.Arrays;
import java.util.function.BiConsumer;

public final class ChangeStream {
    private final IngenuityInput reader;
    private final int stableTicks;
    private final long changeCooldownMs;
    private BiConsumer<Tile[], Tile[]> onChange;
    private Tile[] lastRaw;
    private Tile[] candidate;
    private Tile[] lastStable;
    private int sameCount;
    private long lastChangeAtMillis;

    public ChangeStream(IngenuityInput reader, int stableTicks, long changeCooldownMs, BiConsumer<Tile[], Tile[]> onChange) {
        this.reader = reader;
        this.stableTicks = Math.max(1, stableTicks);
        this.changeCooldownMs = Math.max(0, changeCooldownMs);
        this.onChange = onChange;
    }

    public void tick() {
        Tile[] raw;
        try {
            raw = reader.readSnapshotOnce();
        } catch (Exception e) {
            return;
        }

        if (lastRaw != null && Arrays.equals(raw, lastRaw)) {
            sameCount++;
        } else {
            sameCount = 1;
            candidate = raw;
        }
        lastRaw = raw;

        if (sameCount == stableTicks && IngenuityDebug.enabled()) {
            IngenuityDebug.chat("stabilized ticks=%d", sameCount);
        }

        if (sameCount >= stableTicks) {
            Tile[] currentStable = candidate;
            if (lastStable == null) {
                lastStable = currentStable;
                return;
            }
            if (!Arrays.equals(currentStable, lastStable)) {
                long now = System.currentTimeMillis();
                if ((now - lastChangeAtMillis) >= changeCooldownMs) {
                    if (IngenuityDebug.enabled()) {
                        String hb = lastStable == null ? "-" : SnapshotCodec.hash(lastStable);
                        String ha = SnapshotCodec.hash(currentStable);
                        IngenuityDebug.bar("stable change %s→%s", hb, ha);
                    }
                    if (onChange != null) onChange.accept(lastStable, currentStable);
                    lastStable = currentStable;
                    lastChangeAtMillis = now;
                } else {
                    if (IngenuityDebug.enabled()) {
                        IngenuityDebug.chat("cooldown active");
                    }
                }
            }
        }
    }

    public void reset() {
        lastRaw = null;
        candidate = null;
        lastStable = null;
        sameCount = 0;
        lastChangeAtMillis = 0;
    }

    public Tile[] getLastStable() {
        return lastStable;
    }

    public void setOnChange(BiConsumer<Tile[], Tile[]> onChange) {
        this.onChange = onChange;
    }
}
