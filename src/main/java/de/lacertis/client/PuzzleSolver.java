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
        int[][] dirs = {{0,0}, {-1,0}, {1,0}, {0,-1}, {0,1}};
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

    public static List<Pos> solveWithGaussian(Tile[][] grid, boolean targetOn) {
        List<Pos> triggers = new ArrayList<>();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (grid[x][y].isTrigger) {
                    triggers.add(new Pos(x, y));
                }
            }
        }
        int N = triggers.size();
        Map<Pos, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < N; i++) {
            indexOf.put(triggers.get(i), i);
        }

        List<BitSet> A = new ArrayList<>(N);
        boolean[] b = new boolean[N];
        for (int i = 0; i < N; i++) {
            BitSet row = new BitSet(N);
            Pos p = triggers.get(i);
            // Toggle-Effekt: Self + Nachbarn
            for (int j = 0; j < N; j++) {
                Pos q = triggers.get(j);
                if ((Math.abs(p.x() - q.x()) + Math.abs(p.y() - q.y())) == 0 ||
                        (Math.abs(p.x() - q.x()) + Math.abs(p.y() - q.y())) == 1) {
                    row.set(j);
                }
            }
            A.add(row);
            b[i] = grid[p.x()][p.y()].isLit ^ targetOn;
        }

        int r = 0;
        int[] pivotCol = new int[N];
        Arrays.fill(pivotCol, -1);
        for (int c = 0; c < N && r < N; c++) {
            int sel = -1;
            for (int i = r; i < N; i++) {
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
            for (int i = 0; i < N; i++) {
                if (i != r && A.get(i).get(c)) {
                    A.get(i).xor(A.get(r));
                    b[i] ^= b[r];
                }
            }
            r++;
        }

        for (int i = r; i < N; i++) {
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

    public static List<Pos> solveWithFallback(Tile[][] grid, boolean targetOn) {
        List<Pos> baseSolution = solveWithGaussian(grid, targetOn);
        if (!baseSolution.isEmpty()) {
            return baseSolution;
        }

        List<Pos> bestSolution = new ArrayList<>();
        int maxRandomToggles = 4;
        List<Pos> triggers = new ArrayList<>();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if (grid[x][y].isTrigger) {
                    triggers.add(new Pos(x, y));
                }
            }
        }
        Random rand = new Random();
        for (int attempt = 1; attempt <= maxRandomToggles; attempt++) {
            List<Pos> toggled = new ArrayList<>();
            Tile[][] copy = deepCopyGrid(grid);
            for (int i = 0; i < attempt; i++) {
                Pos p = triggers.get(rand.nextInt(triggers.size()));
                toggleNeighbors(copy, p.x(), p.y());
                toggled.add(p);
            }
            List<Pos> solution = solveWithGaussian(copy, targetOn);
            if (!solution.isEmpty()) {
                bestSolution.addAll(toggled);
                bestSolution.addAll(solution);
                break;
            }
        }
        return bestSolution;
    }

    public static List<Pos> solveAllOnOptimized(Tile[][] grid) {
        return solveWithFallback(grid, true);
    }

    public static List<Pos> solveAllOffOptimized(Tile[][] grid) {
        return solveWithFallback(grid, false);
    }

    // For debugging purposes

    private static void printPuzzle(Tile[][] grid) {
        for (int x = 0; x < 7; x++) {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < 7; y++) {
                sb.append(grid[x][y].isLit ? "1" : "0");
            }
            System.out.println(sb.toString());
        }
    }

    public static void simulateSolution(Tile[][] grid, List<Pos> solution) {
        printPuzzle(grid);
        for (Pos move : solution) {
            toggleNeighbors(grid, move.x(), move.y());
            printPuzzle(grid);
        }
    }
}