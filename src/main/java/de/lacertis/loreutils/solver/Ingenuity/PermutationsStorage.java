package de.lacertis.loreutils.solver.Ingenuity;

import de.lacertis.loreutils.config.ModConfig;
import de.lacertis.loreutils.data.FileManager;
import me.shedaniel.autoconfig.AutoConfig;

import java.io.IOException;
import java.util.*;


public class PermutationsStorage {

    private static final String SUBFOLDER = "ingenuity";
    private static final String FILENAME = "ingenuity.json";
    private static final int MAX_SESSION_LOGS = 10;

    public enum LogMode { OFF, ON_ERROR, VERBOSE }
    private static volatile LogMode LOG_MODE = LogMode.OFF;

    public static void setLogMode(boolean debug) {
        LOG_MODE = debug ? LogMode.VERBOSE : LogMode.OFF;
    }

    public static void setLogMode(LogMode m) {
        LOG_MODE = (m == null ? LogMode.OFF : m);
    }

    public static IngenuityPerms loadOrDefault() {
        try {
            return FileManager.loadJsonInSubfolder(SUBFOLDER, FILENAME, IngenuityPerms.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load or create ingenuity.json", e);
        }
    }

    public static void save(IngenuityPerms d) {
        if (d == null) return;

        while (d.sessionLogs.size() > MAX_SESSION_LOGS) {
            d.sessionLogs.remove(0);
        }

        try {
            FileManager.saveJsonInSubfolder(SUBFOLDER, FILENAME, d);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ingenuity.json", e);
        }
    }

    public static boolean permsReady(IngenuityPerms d) {
        if (d == null || d.stale || d.perms == null) return false;
        if (!isValidPermutation(d.perms.EAST)) return false;
        if (!isValidPermutation(d.perms.WEST)) return false;
        if (!isValidPermutation(d.perms.LECTERN)) return false;
        return runSanity(d);
    }

    public static void setPerm(IngenuityPerms d, Move m, int[] perm) {
        if (d == null || m == null) return;
        if (!isValidPermutation(perm)) {
            throw new IllegalArgumentException("Invalid permutation");
        }

        if (d.perms == null) {
            d.perms = new IngenuityPerms.Perms();
        }

        int[] copy = Arrays.copyOf(perm, perm.length);
        switch (m) {
            case EAST: d.perms.EAST = copy; break;
            case WEST: d.perms.WEST = copy; break;
            case LECTERN: d.perms.LECTERN = copy; break;
        }
    }

    public static boolean runSanity(IngenuityPerms d) {
        if (d == null || d.perms == null) return false;

        boolean east4 = isIdentity(pow(d.perms.EAST, 4));
        boolean west4 = isIdentity(pow(d.perms.WEST, 4));
        boolean lectern2 = isIdentity(pow(d.perms.LECTERN, 2));

        d.sanity.east4IsId = east4;
        d.sanity.west4IsId = west4;
        d.sanity.lectern2IsId = lectern2;

        return east4 && west4 && lectern2;
    }

    public static void markStale(IngenuityPerms d, String reason) {
        if (d == null) return;
        d.stale = true;

        if (reason != null && !reason.isEmpty() && !d.sessionLogs.isEmpty()) {
            IngenuityPerms.SessionLog lastLog = d.sessionLogs.get(d.sessionLogs.size() - 1);
            if (!lastLog.executed.isEmpty()) {
                IngenuityPerms.SessionLog.ExecutedStep lastStep = lastLog.executed.get(lastLog.executed.size() - 1);
                lastStep.note = (lastStep.note == null ? "" : lastStep.note + "; ") + reason;
            }
        }

        save(d);
    }

    public static void addCalibrationRecord(IngenuityPerms d, Move move, String before, String after, int[] perm, String whenIso) {
        if (d == null) return;

        IngenuityPerms.CalibrationRecord rec = new IngenuityPerms.CalibrationRecord();
        rec.move = move.toString();
        rec.before = before;
        rec.after = after;
        rec.perm = Arrays.copyOf(perm, perm.length);
        rec.at = whenIso;

        d.calibrationHistory.add(rec);
        save(d);
    }

    public static void addSessionLogStart(IngenuityPerms d, String runId, String startedAt, List<String> planned) {
        if (d == null || LOG_MODE == LogMode.OFF) return;
        if (LOG_MODE == LogMode.ON_ERROR) return;

        IngenuityPerms.SessionLog log = new IngenuityPerms.SessionLog();
        log.runId = runId;
        log.startedAt = startedAt;
        log.planned = new ArrayList<>(planned);

        d.sessionLogs.add(log);
        save(d);
    }

    public static void addSessionLogStep(IngenuityPerms d, String runId, String move, String beforeHash, String afterHash, boolean expectedMatch, String note) {
        if (d == null || LOG_MODE == LogMode.OFF) return;

        if (LOG_MODE == LogMode.ON_ERROR && expectedMatch && (note == null || note.isEmpty())) {
            return;
        }

        for (int i = d.sessionLogs.size() - 1; i >= 0; i--) {
            IngenuityPerms.SessionLog log = d.sessionLogs.get(i);
            if (runId.equals(log.runId)) {
                IngenuityPerms.SessionLog.ExecutedStep step = new IngenuityPerms.SessionLog.ExecutedStep();
                step.move = move;
                step.beforeHash = beforeHash;
                step.afterHash = afterHash;
                step.expectedMatch = expectedMatch;
                step.note = note;

                log.executed.add(step);
                save(d);
                return;
            }
        }
    }

    public static void addGoal(IngenuityPerms d, String snapshotCodec, String note, String whenIso) {
        if (d == null) return;

        IngenuityPerms.GoalEntry goal = new IngenuityPerms.GoalEntry();
        goal.snapshot = snapshotCodec;
        goal.note = note;
        goal.savedAt = whenIso;

        d.goals.add(goal);
        save(d);
    }

    public static List<String> listGoals(IngenuityPerms d) {
        if (d == null) return Collections.emptyList();

        List<String> result = new ArrayList<>();
        for (IngenuityPerms.GoalEntry g : d.goals) {
            result.add(g.snapshot);
        }
        return result;
    }

    private static boolean isValidPermutation(int[] p) {
        if (p == null || p.length != 8) return false;

        boolean[] seen = new boolean[8];
        for (int v : p) {
            if (v < 0 || v >= 8 || seen[v]) return false;
            seen[v] = true;
        }
        return true;
    }

    private static int[] identity() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    private static int[] compose(int[] a, int[] b) {
        int[] result = new int[8];
        for (int i = 0; i < 8; i++) {
            result[i] = a[b[i]];
        }
        return result;
    }

    private static int[] pow(int[] p, int k) {
        if (p == null || k < 0) return null;
        if (k == 0) return identity();

        int[] result = identity();
        for (int i = 0; i < k; i++) {
            result = compose(p, result);
        }
        return result;
    }

    private static boolean isIdentity(int[] p) {
        if (p == null || p.length != 8) return false;
        for (int i = 0; i < 8; i++) {
            if (p[i] != i) return false;
        }
        return true;
    }

    static {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        setLogMode(config.ingenuityDebug);
    }
}
