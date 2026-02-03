import java.util.ArrayDeque;
import java.util.ArrayList;

public class BFS {
    private final LongVisitedSet visited;
    private static final int GOAL_SQUARE = 6;
    private static final char[] DIRS = {'u','d','l','r'};

    public BFS(int expectedStates) {
        visited = new LongVisitedSet(expectedStates);
    }

    public boolean markExplored(Board board) {
        long key = StateEncoder.encodeState(board.getPieces());
        return visited.addIfAbsent(key); // true = newly discovered
    }

    private static boolean isGoal(Board b) {
        for (Piece p : b.getPieces()) {
            if (p.getType() == Piece.TWO_BY_TWO) {
                return p.getTopLeft() == GOAL_SQUARE;
            }
        }
        throw new IllegalStateException("No 2x2 piece found.");
    }

    public int solve(Board start) {
        ArrayDeque<Node> q = new ArrayDeque<>();

        long startKey = StateEncoder.encodeState(start.getPieces());
        visited.addIfAbsent(startKey);
        q.add(new Node(start, 0));

        while (!q.isEmpty()) {
            Node cur = q.removeFirst();
            if (isGoal(cur.board)) return cur.depth;

            // Generate neighbors
            ArrayList<Piece> curPieces = cur.board.getPieces();
            for (int i = 0; i < curPieces.size(); i++) {
                for (char dir : DIRS) {
                    Board next = new Board(cur.board);

                    // IMPORTANT: get the corresponding piece from the copied board
                    Piece moved = next.getPieces().get(i);

                    if (!next.movePiece(moved, dir)) continue;

                    long key = StateEncoder.encodeState(next.getPieces());
                    if (visited.addIfAbsent(key)) {
                        q.addLast(new Node(next, cur.depth + 1));
                    }
                }
            }
        }

        return -1; // no solution
    }

    private static final class Node {
        final Board board;
        final int depth;
        Node(Board board, int depth) {
            this.board = board;
            this.depth = depth;
        }
    }

    public static void main(String[] args) {
        Board start = new Board();

        BFS bfs = new BFS(200_000); // rough guess; it resizes anyway
        int moves = bfs.solve(start);

        System.out.println("Shortest solution length: " + moves);
    }

    private static final class LongVisitedSet {
        private long[] table;   // stores key+1; 0 = empty
        private int size;
        private int resizeAt;

        public LongVisitedSet(int expectedSize) {
            int cap = 1;
            while (cap < (expectedSize * 10) / 7) cap <<= 1; // ~0.7 load factor
            table = new long[cap];
            resizeAt = (cap * 7) / 10;
        }

        /** returns true if newly added, false if already present */
        public boolean addIfAbsent(long key) {
            long stored = key + 1;
            if (stored == 0) throw new IllegalArgumentException("Key overflow"); // basically impossible

            if (size >= resizeAt) rehash();

            int mask = table.length - 1;
            int idx = (int) mix64(key) & mask;

            while (true) {
                long cur = table[idx];
                if (cur == 0) {
                    table[idx] = stored;
                    size++;
                    return true;
                }
                if (cur == stored) return false;
                idx = (idx + 1) & mask;
            }
        }

        private void rehash() {
            long[] old = table;
            table = new long[old.length << 1];
            size = 0;
            resizeAt = (table.length * 7) / 10;

            int mask = table.length - 1;
            for (long stored : old) {
                if (stored == 0) continue;
                long key = stored - 1;
                int idx = (int) mix64(key) & mask;
                while (table[idx] != 0) idx = (idx + 1) & mask;
                table[idx] = stored;
                size++;
            }
        }

        private static long mix64(long z) {
            z ^= (z >>> 33);
            z *= 0xff51afd7ed558ccdL;
            z ^= (z >>> 33);
            z *= 0xc4ceb9fe1a85ec53L;
            z ^= (z >>> 33);
            return z;
        }
    }

}
