package de.lacertis.loreutils.solver;

import java.util.*;

public class LightsOutSolver {

    public record Pos(int x, int y) {}

    public static class Tile {
        public boolean isTrigger;
        public boolean isLit;
        public Tile(boolean isTrigger, boolean isLit) {
            this.isTrigger = isTrigger;
            this.isLit = isLit;
        }
    }

    public static List<Pos> solveFullGrid(Tile[][] grid, int[][] targetGrid) {
        List<Pos> triggers = new ArrayList<>();
        for (int x = 0; x < 7; x++)
            for (int y = 0; y < 7; y++)
                if (grid[x][y].isTrigger)
                    triggers.add(new Pos(x, y));
        int N = triggers.size();
        List<BitSet> A = new ArrayList<>(N);
        boolean[] b = new boolean[N];

        for (int i = 0; i < N; i++) {
            Pos p = triggers.get(i);
            BitSet row = new BitSet(N);
            for (int j = 0; j < N; j++) {
                Pos q = triggers.get(j);
                if (Math.abs(q.x() - p.x()) + Math.abs(q.y() - p.y()) <= 1)
                    row.set(j);
            }
            A.add(row);
            b[i] = grid[p.x()][p.y()].isLit ^ (targetGrid[p.x()][p.y()] == 1);
        }

        int[] pivot = new int[N];
        Arrays.fill(pivot, -1);
        int r = 0;
        for (int c = 0; c < N && r < N; c++) {
            int sel = r;
            while (sel < N && !A.get(sel).get(c)) sel++;
            if (sel == N) continue;
            Collections.swap(A, r, sel);
            boolean tmp = b[r]; b[r] = b[sel]; b[sel] = tmp;
            pivot[r] = c;
            for (int i = 0; i < N; i++) {
                if (i != r && A.get(i).get(c)) {
                    A.get(i).xor(A.get(r));
                    b[i] ^= b[r];
                }
            }
            r++;
        }

        for (int i = r; i < N; i++) {
            if (A.get(i).isEmpty() && b[i]) return Collections.emptyList();
        }

        boolean[] sol = new boolean[N];
        for (int i = r - 1; i >= 0; i--) {
            int c = pivot[i];
            if (c < 0) continue;
            boolean v = b[i];
            for (int j = c + 1; j < N; j++)
                if (A.get(i).get(j) && sol[j])
                    v ^= true;
            sol[c] = v;
        }

        List<Pos> result = new ArrayList<>();
        for (int i = 0; i < N; i++)
            if (sol[i])
                result.add(triggers.get(i));
        return result;
    }

    public static List<Pos> solveAllOnOptimized(Tile[][] grid) {
        int[][] targetGrid = new int[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                targetGrid[x][y] = LightsOutInput.triggerLayout[x][y] ? 1 : 0;
            }
        }
        return solveFullGrid(grid, targetGrid);
    }

    public static List<Pos> solveAllOffOptimized(Tile[][] grid) {
        int[][] targetGrid = new int[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                targetGrid[x][y] = LightsOutInput.triggerLayout[x][y] ? 0 : 0;
            }
        }
        return solveFullGrid(grid, targetGrid);
    }

    public static List<Pos> solveStrengthOptimized(Tile[][] grid) {
        int[][] targetGrid = new int[7][7];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                targetGrid[x][y] = (x == 3 && y == 3) ? 1 : 0;
            }
        }
        return solveFullGrid(grid, targetGrid);
    }

}