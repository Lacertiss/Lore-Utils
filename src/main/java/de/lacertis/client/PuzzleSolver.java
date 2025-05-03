package de.lacertis.client;

import java.util.*;

public class PuzzleSolver {

    public record Pos(int x, int y) {}

    public static class Tile {
        public boolean isTrigger;
        public boolean isLit;
        public Tile(boolean isTrigger, boolean isLit) {
            this.isTrigger = isTrigger;
            this.isLit = isLit;
        }
    }

    private static void toggleIfExists(Tile[][] grid, int x, int y) {
        if (x >= 0 && x < grid.length && y >= 0 && y < grid[x].length && grid[x][y].isTrigger) {
            grid[x][y].isLit = !grid[x][y].isLit;
        }
    }

    public static void toggleNeighbors(Tile[][] grid, int x, int y) {
        int[][] dirs = { {0,0}, {-1,0}, {1,0}, {0,-1}, {0,1} };
        for (int[] d : dirs) {
            toggleIfExists(grid, x + d[0], y + d[1]);
        }
    }

    private static Tile[][] deepCopyGrid(Tile[][] grid) {
        Tile[][] copy = new Tile[grid.length][grid[0].length];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Tile t = grid[x][y];
                copy[x][y] = new Tile(t.isTrigger, t.isLit);
            }
        }
        return copy;
    }

    public static List<Pos> solveFullGrid(Tile[][] grid, boolean targetOn) {
        List<Pos> triggers = new ArrayList<>();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (grid[x][y].isTrigger) {
                    triggers.add(new Pos(x, y));
                }
            }
        }
        int N = triggers.size();

        List<BitSet> A = new ArrayList<>(49);
        boolean[] b = new boolean[49];
        for (int i = 0; i < 49; i++) {
            int row = i / 7;
            int col = i % 7;
            BitSet rowBits = new BitSet(N);
            for (int j = 0; j < N; j++) {
                Pos t = triggers.get(j);
                if (Math.abs(t.x() - row) + Math.abs(t.y() - col) <= 1) {
                    rowBits.set(j);
                }
            }
            A.add(rowBits);
            b[i] = grid[row][col].isLit ^ targetOn;
        }

        int[] pivotCol = new int[49];
        Arrays.fill(pivotCol, -1);
        int r = 0;
        for (int c = 0; c < N && r < 49; c++) {
            int sel = -1;
            for (int i = r; i < 49; i++) {
                if (A.get(i).get(c)) {
                    sel = i;
                    break;
                }
            }
            if (sel < 0) continue;
            Collections.swap(A, r, sel);
            boolean tmp = b[r];
            b[r] = b[sel];
            b[sel] = tmp;
            pivotCol[r] = c;
            for (int i = 0; i < 49; i++) {
                if (i != r && A.get(i).get(c)) {
                    A.get(i).xor(A.get(r));
                    b[i] ^= b[r];
                }
            }
            r++;
        }

        for (int i = r; i < 49; i++) {
            int row = i / 7, col = i % 7;
            if (!grid[row][col].isTrigger) {
                continue;
            }
            if (A.get(i).isEmpty() && b[i]) {
                return new ArrayList<>();
            }
        }

        boolean[] sol = new boolean[N];
        for (int i = r - 1; i >= 0; i--) {
            int c = pivotCol[i];
            if (c < 0) continue;
            boolean val = b[i];
            for (int j = c + 1; j < N; j++) {
                if (A.get(i).get(j) && sol[j]) {
                    val ^= true;
                }
            }
            sol[c] = val;
        }

        List<Pos> result = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            if (sol[i]) {
                result.add(triggers.get(i));
            }
        }
        return result;
    }

    public static List<Pos> solveAllOnOptimized(Tile[][] grid) {
        return solveFullGrid(grid, true);
    }

    public static List<Pos> solveAllOffOptimized(Tile[][] grid) {
        return solveFullGrid(grid, false);
    }
}