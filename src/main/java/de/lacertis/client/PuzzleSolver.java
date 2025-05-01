package de.lacertis.client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PuzzleSolver {

    public static class Tile {
        public boolean isTrigger;
        public boolean isLit;
        public Tile(boolean isTrigger, boolean isLit) {
            this.isTrigger = isTrigger;
            this.isLit = isLit;
        }
    }

    private static void toggle(Tile[][] grid, int x, int y) {
        if (!grid[x][y].isTrigger) return;
        grid[x][y].isLit = !grid[x][y].isLit;
        if (x > 0 && grid[x - 1][y].isTrigger) grid[x - 1][y].isLit = !grid[x - 1][y].isLit;
        if (x < 6 && grid[x + 1][y].isTrigger) grid[x + 1][y].isLit = !grid[x + 1][y].isLit;
        if (y > 0 && grid[x][y - 1].isTrigger) grid[x][y - 1].isLit = !grid[x][y - 1].isLit;
        if (y < 6 && grid[x][y + 1].isTrigger) grid[x][y + 1].isLit = !grid[x][y + 1].isLit;
    }

    public static List<Point> solveAllOnOptimized(Tile[][] grid) {
        int size = 49;
        int[][] matrix = new int[size][size];
        int[] b = new int[size];
        for (int i = 0; i < size; i++) {
            int x = i / 7, y = i % 7;
            if (!grid[x][y].isTrigger) {
                b[i] = 0;
                continue;
            }
            b[i] = grid[x][y].isLit ? 0 : 1;
            setMatrixRow(matrix, i, x, y);
        }
        int[] solution = solveGF2(matrix, b);
        List<Point> path = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (solution[i] == 1) {
                int x = i / 7, y = i % 7;
                if (grid[x][y].isTrigger) {
                    path.add(new Point(x, y));
                }
            }
        }
        return path;
    }

    public static List<Point> solveAllOffOptimized(Tile[][] grid) {
        int size = 49;
        int[][] matrix = new int[size][size];
        int[] b = new int[size];
        for (int i = 0; i < size; i++) {
            int x = i / 7, y = i % 7;
            b[i] = grid[x][y].isLit ? 1 : 0;
            setMatrixRow(matrix, i, x, y);
        }
        int[] solution = solveGF2(matrix, b);
        List<Point> path = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (solution[i] == 1) {
                int x = i / 7, y = i % 7;
                path.add(new Point(x, y));
            }
        }
        return path;
    }

    private static void setMatrixRow(int[][] matrix, int row, int x, int y) {
        int[] dx = {0, 1, -1, 0, 0};
        int[] dy = {0, 0, 0, 1, -1};
        for (int k = 0; k < dx.length; k++) {
            int nx = x + dx[k], ny = y + dy[k];
            if (nx >= 0 && nx < 7 && ny >= 0 && ny < 7) {
                int col = nx * 7 + ny;
                matrix[row][col] = 1;
            }
        }
    }

    private static int[] solveGF2(int[][] mat, int[] b) {
        int n = mat.length;
        int[] x = new int[n];
        int row = 0;
        for (int col = 0; col < n && row < n; col++) {
            int pivot = row;
            while (pivot < n && mat[pivot][col] == 0) pivot++;
            if (pivot == n) continue;
            int[] tmp = mat[row];
            mat[row] = mat[pivot];
            mat[pivot] = tmp;
            int tb = b[row];
            b[row] = b[pivot];
            b[pivot] = tb;
            for (int r = row + 1; r < n; r++) {
                if (mat[r][col] == 1) {
                    for (int c = col; c < n; c++) {
                        mat[r][c] ^= mat[row][c];
                    }
                    b[r] ^= b[row];
                }
            }
            row++;
        }
        for (int r = n - 1; r >= 0; r--) {
            int pivotCol = -1;
            for (int c = 0; c < n; c++) {
                if (mat[r][c] == 1) {
                    pivotCol = c;
                    break;
                }
            }
            if (pivotCol == -1) continue;
            int val = b[r];
            for (int c = pivotCol + 1; c < n; c++) {
                val ^= (mat[r][c] & x[c]);
            }
            x[pivotCol] = val;
        }
        return x;
    }

    public static void main(String[] args) {
        boolean[][] lightStates = {
                {false,true,false,true,false,true,false},
                {false,true,true,true,true,true,false},
                {false,false,true,false,true,false,false},
                {false,false,true,false,false,false,false},
                {false,false,false,true,true,true,false},
                {true,false,false,false,true,false,true},
                {false,false,false,true,false,false,false}
        };
        Tile[][] grid = PuzzleInput.createGridFromLights(lightStates);
        System.out.println("L\u00f6sung AN (Optimized): " + solveAllOnOptimized(grid));
        System.out.println("L\u00f6sung AUS (Optimized): " + solveAllOffOptimized(grid));
    }
}