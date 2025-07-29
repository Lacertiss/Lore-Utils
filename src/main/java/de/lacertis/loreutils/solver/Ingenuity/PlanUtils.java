package de.lacertis.loreutils.solver.Ingenuity;

import java.util.ArrayList;
import java.util.List;

public final class PlanUtils {

    public static class Run {
        public final Move move;
        public final int count;

        public Run(Move move, int count) {
            this.move = move;
            this.count = count;
        }

        @Override
        public String toString() {
            return move + " x" + count;
        }
    }

    public static List<Run> compress(List<Move> path) {
        List<Run> runs = new ArrayList<>();
        if (path == null || path.isEmpty()) return runs;

        Move current = path.get(0);
        int count = 1;

        for (int i = 1; i < path.size(); i++) {
            if (path.get(i) == current) {
                count++;
            } else {
                runs.add(new Run(current, count));
                current = path.get(i);
                count = 1;
            }
        }
        runs.add(new Run(current, count));

        return runs;
    }

    public static String toCompactString(List<Run> runs) {
        if (runs == null || runs.isEmpty()) return "(empty)";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(runs.get(i).toString());
        }
        return sb.toString();
    }
}
