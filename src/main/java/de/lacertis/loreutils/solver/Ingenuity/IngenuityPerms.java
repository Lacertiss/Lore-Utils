package de.lacertis.loreutils.solver.Ingenuity;

import java.util.ArrayList;
import java.util.List;

public class IngenuityPerms {
    public int schemaVersion = 1;
    public String modVersion;
    public String learnedAt;
    public boolean stale = true;

    public Perms perms;
    public Sanity sanity = new Sanity();
    public List<GoalEntry> goals = new ArrayList<>();
    public List<CalibrationRecord> calibrationHistory = new ArrayList<>();
    public List<SessionLog> sessionLogs = new ArrayList<>();

    public IngenuityPerms() { }

    public static class Perms {
        public int[] EAST;
        public int[] WEST;
        public int[] LECTERN;
    }

    public static class Sanity {
        public boolean east4IsId;
        public boolean west4IsId;
        public boolean lectern2IsId;
    }

    public static class GoalEntry {
        public String snapshot;
        public String savedAt;
        public String note;
    }

    public static class CalibrationRecord {
        public String move;
        public String before;
        public String after;
        public int[] perm;
        public String at;
    }

    public static class SessionLog {
        public String runId;
        public String startedAt;
        public List<String> planned = new ArrayList<>();
        public List<ExecutedStep> executed = new ArrayList<>();

        public static class ExecutedStep {
            public String move;
            public String beforeHash;
            public String afterHash;
            public boolean expectedMatch;
            public String note;
        }
    }
}
