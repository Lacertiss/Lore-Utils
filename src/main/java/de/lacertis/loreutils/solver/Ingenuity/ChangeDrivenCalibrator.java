package de.lacertis.loreutils.solver.Ingenuity;

import de.lacertis.loreutils.MessageManager;

import java.time.Instant;
import java.util.*;

public final class ChangeDrivenCalibrator {
    private final ChangeStream changeStream;
    private final EnumMap<Move, List<Tile[]>> sessions = new EnumMap<>(Move.class);
    private final Map<Move, Integer> expectedOrder = Map.of(Move.EAST, 4, Move.WEST, 4, Move.LECTERN, 2);
    private final int backtrackLimit = 100000;
    private boolean running;

    public ChangeDrivenCalibrator(IngenuityInput reader) {
        this.changeStream = new ChangeStream(reader, 2, 200, this::onChange);
    }

    public void start() { running = true; }
    public void stop() { running = false; }
    public boolean isRunning() { return running; }
    public void tick() { if (running) changeStream.tick(); }
    public void resetSessions() { sessions.clear(); }

    private void onChange(Tile[] before, Tile[] after) {
        Tile[] beforeCopy = Arrays.copyOf(before, before.length);
        Tile[] afterCopy = Arrays.copyOf(after,  after.length);
        String hBefore = IngenuityDebug.hash(beforeCopy);
        String hAfter = IngenuityDebug.hash(afterCopy);

        EnumSet<Slot> delta = DeltaUtil.diff(beforeCopy, afterCopy);
        Move move = DeltaUtil.classify(delta);

        if (IngenuityDebug.enabled()) {
            IngenuityDebug.chat("Δ=%d move=%s", delta.size(), move);
            IngenuityDebug.bar("Δ:%d %s", delta.size(), IngenuityDebug.slots(delta));
        }

        if (move == Move.UNKNOWN) {
            if (IngenuityDebug.enabled()) {
                IngenuityDebug.chat("classify=UNKNOWN Δ=%d %s", delta.size(), IngenuityDebug.slots(delta));
            }
            return;
        }

        List<Tile[]> snaps = sessions.computeIfAbsent(move, k -> new ArrayList<>());
        if (snaps.isEmpty()) {
            snaps.add(beforeCopy);
            snaps.add(afterCopy);
        } else if (!Arrays.equals(afterCopy, snaps.get(snaps.size() - 1))) {
            snaps.add(afterCopy);
        }
        while (snaps.size() > 16) snaps.remove(0);

        if (IngenuityDebug.enabled()) {
            int len = snaps.size();
            IngenuityDebug.chat("session[%s].len=%d before=%s after=%s", move, len, hBefore, hAfter);
        }

        PermutationCore.Result res = PermutationCore.inferPermutation(snaps, expectedOrder.get(move), backtrackLimit);

        if (res.reason != null) {
            if (IngenuityDebug.enabled()) {
                IngenuityDebug.chat("infer[%s] reason=%s explored=%d", move, res.reason, res.explored);
            }
        } else {
            if (IngenuityDebug.enabled()) {
                IngenuityDebug.chat("learned[%s] perm=%s", move, Arrays.toString(res.perm));
            }
        }

        if (res.reason == null && res.unique && res.perm != null) {
            IngenuityPerms d = PermutationsStorage.loadOrDefault();
            PermutationsStorage.setPerm(d, move, res.perm);

            MessageManager.sendChatColored("&bIngenuity: Learned &f" + move + "&b permutation (" + res.explored + " nodes).");

            boolean ok = PermutationsStorage.runSanity(d);

            if (IngenuityDebug.enabled()) {
                IngenuityDebug.chat("sanity E4=%s W4=%s L2=%s stale=%s", d.sanity.east4IsId, d.sanity.west4IsId, d.sanity.lectern2IsId, d.stale);
            }

            if (ok && d.perms != null && d.perms.EAST != null && d.perms.WEST != null && d.perms.LECTERN != null) {
                d.stale = false;
                d.learnedAt = Instant.now().toString();
                MessageManager.sendChatColored("&aIngenuity: Calibration complete. Sanity: east4=" + d.sanity.east4IsId + ", west4=" + d.sanity.west4IsId + ", lectern2=" + d.sanity.lectern2IsId + ".");
            }
            PermutationsStorage.save(d);
            while (snaps.size() > 2) snaps.remove(0);
            if (!d.stale) running = false;
        } else if ("inconsistent".equals(res.reason) || "backtrack-limit".equals(res.reason)) {
            sessions.remove(move);
            MessageManager.sendChatColored("&cIngenuity: Calibration reset for " + move + " (" + res.reason + "). Try again.");
        }
    }
}
