package de.lacertis.loreutils.solver.Ingenuity;

import java.util.*;

public class IngenuitySolver {

    public List<Move> solve(Tile[] start, Goal goal, IngenuityPerms perms, int maxNodes) {
        if (start == null || start.length != 8) throw new IllegalArgumentException("Invalid start");
        if (goal == null) throw new IllegalArgumentException("Goal is null");
        if (perms == null || !PermutationsStorage.permsReady(perms)) throw new IllegalArgumentException("Perms not ready");
        if (maxNodes <= 0) throw new IllegalArgumentException("Invalid maxNodes");

        Set<String> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();

        String startKey = key(start);
        visited.add(startKey);
        queue.add(new Node(start, new ArrayList<>()));

        int nodeCount = 0;

        while (!queue.isEmpty()) {
            if (++nodeCount > maxNodes) return null;

            Node current = queue.poll();

            for (Move move : new Move[]{Move.EAST, Move.WEST, Move.LECTERN}) {
                Tile[] neighbor = applyMove(current.snapshot, move, perms);

                if (goal.isSatisfied(neighbor)) {
                    List<Move> path = new ArrayList<>(current.path);
                    path.add(move);
                    return path;
                }

                String neighborKey = key(neighbor);
                if (!visited.contains(neighborKey)) {
                    visited.add(neighborKey);
                    List<Move> newPath = new ArrayList<>(current.path);
                    newPath.add(move);
                    queue.add(new Node(neighbor, newPath));
                }
            }
        }

        return null;
    }

    private static Tile[] applyMove(Tile[] s, Move m, IngenuityPerms d) {
        switch (m) {
            case EAST: return PermutationCore.apply(d.perms.EAST, s);
            case WEST: return PermutationCore.apply(d.perms.WEST, s);
            case LECTERN: return PermutationCore.apply(d.perms.LECTERN, s);
            default: throw new IllegalArgumentException("Invalid move");
        }
    }

    private static String key(Tile[] s) {
        return SnapshotCodec.hash(s);
    }

    private static class Node {
        final Tile[] snapshot;
        final List<Move> path;

        Node(Tile[] snapshot, List<Move> path) {
            this.snapshot = snapshot;
            this.path = path;
        }
    }
}
