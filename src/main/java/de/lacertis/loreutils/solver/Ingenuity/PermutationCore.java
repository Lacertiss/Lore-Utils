package de.lacertis.loreutils.solver.Ingenuity;

import java.util.*;

public final class PermutationCore {

    private static final int SIZE = 8;

    private PermutationCore() {
    }

    public static final class Result {
        public final int[] perm;
        public final boolean unique;
        public final int explored;
        public final String reason;

        public Result(int[] perm, boolean unique, int explored, String reason) {
            this.perm = perm == null ? null : Arrays.copyOf(perm, perm.length);
            this.unique = unique;
            this.explored = explored;
            this.reason = reason;
        }
    }

    public static Tile[] apply(int[] perm, Tile[] in) {
        if (perm == null || perm.length != SIZE) throw new IllegalArgumentException("Invalid permutation");
        if (in == null || in.length != SIZE) throw new IllegalArgumentException("Invalid input array");
        Tile[] out = new Tile[SIZE];
        for (int i = 0; i < SIZE; i++) out[perm[i]] = in[i];
        return out;
    }

    public static boolean equalTiles(Tile[] a, Tile[] b) {
        if (a == b) return true;
        if (a == null || b == null || a.length != SIZE || b.length != SIZE) return false;
        for (int i = 0; i < SIZE; i++) if (a[i] != b[i]) return false;
        return true;
    }

    public static int[] compose(int[] a, int[] b) {
        if (a == null || b == null || a.length != SIZE || b.length != SIZE)
            throw new IllegalArgumentException("Invalid permutations");
        int[] r = new int[SIZE];
        for (int i = 0; i < SIZE; i++) r[i] = a[b[i]];
        return r;
    }

    public static int[] pow(int[] p, int k) {
        if (p == null || p.length != SIZE) throw new IllegalArgumentException("Invalid permutation");
        if (k < 0) throw new IllegalArgumentException("Negative exponent");
        int[] r = identity();
        for (int i = 0; i < k; i++) r = compose(p, r);
        return r;
    }

    public static boolean isIdentity(int[] p) {
        if (p == null || p.length != SIZE) return false;
        for (int i = 0; i < SIZE; i++) if (p[i] != i) return false;
        return true;
    }

    public static int order(int[] p, int maxK) {
        if (!isValidPermutation(p)) return -1;
        int[] c = Arrays.copyOf(p, SIZE);
        for (int k = 1; k <= maxK; k++) {
            if (isIdentity(c)) return k;
            c = compose(p, c);
        }
        return -1;
    }

    public static Result inferPermutation(List<Tile[]> snaps, Integer expectedOrder, int maxBacktracks) {
        if (snaps == null || snaps.size() < 2) return new Result(null, false, 0, "need-more-data");
        for (Tile[] s : snaps) if (s == null || s.length != SIZE) return new Result(null, false, 0, "invalid-snapshot");

        List<Set<Integer>> cand = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            Set<Integer> set = new HashSet<>();
            for (int j = 0; j < SIZE; j++) set.add(j);
            cand.add(set);
        }

        for (int t = 0; t < snaps.size() - 1; t++) {
            Tile[] from = snaps.get(t);
            Tile[] to = snaps.get(t + 1);
            for (int i = 0; i < SIZE; i++) {
                Set<Integer> ref = new HashSet<>();
                for (int j : cand.get(i)) if (to[j] == from[i]) ref.add(j);
                cand.set(i, ref);
                if (ref.isEmpty()) return new Result(null, false, 0, "empty-candidate-set@" + i);
            }
        }

        Integer[] orderIdx = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) orderIdx[i] = i;
        Arrays.sort(orderIdx, (a, b) -> {
            int da = cand.get(a).size(), db = cand.get(b).size();
            return da != db ? Integer.compare(da, db) : Integer.compare(a, b);
        });


        BacktrackState st = new BacktrackState(cand, snaps, expectedOrder, maxBacktracks);
        List<int[]> sols = new ArrayList<>();
        int[] partial = new int[SIZE];
        Arrays.fill(partial, -1);
        backtrack(st, partial, new boolean[SIZE], orderIdx, 0, sols);

        if (st.explored >= maxBacktracks) return new Result(null, false, st.explored, "backtrack-limit");
        if (sols.isEmpty()) return new Result(null, false, st.explored, "inconsistent");
        if (sols.size() > 1) return new Result(null, false, st.explored, "ambiguous");

        int[] perm = sols.get(0);
        if (expectedOrder != null) {
            if (!isIdentity(pow(perm, expectedOrder))) return new Result(null, true, st.explored, "order-check-failed");
        } else {
            if (order(perm, 8) == -1) return new Result(null, true, st.explored, "order-check-failed");
        }
        return new Result(perm, true, st.explored, null);
    }

    public static boolean isValidPermutation(int[] p) {
        if (p == null || p.length != SIZE) return false;
        boolean[] seen = new boolean[SIZE];
        for (int v : p) if (v < 0 || v >= SIZE || seen[v]) return false; else seen[v] = true;
        return true;
    }

    public static int[] identity() {
        int[] id = new int[SIZE];
        for (int i = 0; i < SIZE; i++) id[i] = i;
        return id;
    }

    private static class BacktrackState {
        final List<Set<Integer>> candidates;
        final List<Tile[]> snaps;
        final Integer expectedOrder;
        final int maxBacktracks;
        int explored = 0;

        BacktrackState(List<Set<Integer>> candidates, List<Tile[]> snaps, Integer expectedOrder, int maxBacktracks) {
            this.candidates = candidates;
            this.snaps = snaps;
            this.expectedOrder = expectedOrder;
            this.maxBacktracks = maxBacktracks;
        }
    }

    private static void backtrack(BacktrackState st, int[] perm, boolean[] used, Integer[] idx, int depth, List<int[]> sols) {
        if (sols.size() > 1 || st.explored >= st.maxBacktracks) return;
        st.explored++;
        if (depth == SIZE) {
            if (verifyPermutation(perm, st.snaps)) sols.add(Arrays.copyOf(perm, SIZE));
            return;
        }
        int i = idx[depth];
        List<Integer> candList = new ArrayList<>(st.candidates.get(i));
        Collections.sort(candList);
        for (int j : candList) {
            if (used[j]) continue;
            perm[i] = j;
            used[j] = true;
            if (forwardCheck(perm, i, st)) backtrack(st, perm, used, idx, depth + 1, sols);
            used[j] = false;
            perm[i] = -1;
            if (sols.size() > 1 || st.explored >= st.maxBacktracks) return;
        }
    }

    private static boolean forwardCheck(int[] perm, int assignedIndex, BacktrackState st) {
        int maxK = Math.min(st.snaps.size() - 1, SIZE);
        for (int k = 2; k <= maxK; k++) {
            int idx = assignedIndex;
            boolean complete = true;
            for (int step = 0; step < k; step++) {
                if (perm[idx] == -1) {
                    complete = false;
                    break;
                }
                idx = perm[idx];
            }
            if (complete && st.snaps.get(k)[idx] != st.snaps.get(0)[assignedIndex]) return false;
        }
        if (st.expectedOrder != null) {
            int idx = assignedIndex;
            boolean complete = true;
            for (int step = 0; step < st.expectedOrder; step++) {
                if (perm[idx] == -1) {
                    complete = false;
                    break;
                }
                idx = perm[idx];
            }
            if (complete && idx != assignedIndex) return false;
        }
        return true;
    }

    private static boolean verifyPermutation(int[] perm, List<Tile[]> snaps) {
        for (int t = 0; t < snaps.size() - 1; t++) if (!equalTiles(apply(perm, snaps.get(t)), snaps.get(t + 1))) return false;
        return true;
    }
}
