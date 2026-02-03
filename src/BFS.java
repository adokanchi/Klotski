import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

public class BFS {
    private final LongVisitedSet visited;
    private final int goalSquare;
    private static final char[] DIRS = {'u','d','l','r'};

    public BFS(int expectedStates, int goalSquare) {
        visited = new LongVisitedSet(expectedStates);
        this.goalSquare = goalSquare;
    }

    private boolean isGoal(Board b) {
        for (Piece p : b.getPieces()) {
            if (p.getType() == Piece.TWO_BY_TWO) {
                return p.getTopLeft() == goalSquare;
            }
        }
        throw new IllegalStateException("No 2x2 piece found.");
    }

    public ArrayList<Move> solve(Board start) {
        ArrayDeque<Node> q = new ArrayDeque<>();

        long startKey = StateEncoder.encodeState(start.getPieces());
        visited.addIfAbsent(startKey, LongVisitedSet.ROOT_PARENT, LongVisitedSet.ROOT_MOVE);
        q.add(new Node(start, startKey));

        while (!q.isEmpty()) {
            Node cur = q.removeFirst();
            if (isGoal(cur.board)) return reconstructMoves(startKey, cur.key);

            // Generate neighbors
            ArrayList<Piece> curPieces = cur.board.getPieces();
            for (int i = 0; i < curPieces.size(); i++) {
                Piece p = curPieces.get(i);
                int type = p.getType();
                int fromTopLeft = p.getTopLeft();

                for (char dir : DIRS) {
                    Board next = new Board(cur.board);
                    Piece moved = next.getPieces().get(i);

                    if (!next.movePiece(moved, dir)) continue;

                    long nextKey = StateEncoder.encodeState(next.getPieces());
                    int moveCode = packMove(type, fromTopLeft, dir);
                    if (visited.addIfAbsent(nextKey, cur.key, moveCode)) {
                        q.addLast(new Node(next, nextKey));
                    }
                }
            }
        }

        return null; // no solution
    }

    private ArrayList<Move> reconstructMoves(long startKey, long goalKey) {
        ArrayList<Move> reversed = new ArrayList<>();
        long k = goalKey;

        while (k != startKey) {
            int code = visited.getMoveCode(k);
            reversed.add(unpackMove(code));
            k = visited.getParent(k);
        }

        Collections.reverse(reversed);
        return reversed;
    }

    private static int packMove(int type, int fromTopLeft, char dir) {
        int d = switch (dir) {
            case 'u' -> 0;
            case 'd' -> 1;
            case 'l' -> 2;
            case 'r' -> 3;
            default -> throw new IllegalArgumentException("Bad dir: " + dir);
        };
        return (type & 3) | ((fromTopLeft & 31) << 2) | ((d & 3) << 7);
    }

    private static Move unpackMove(int code) {
        int type = code & 3;
        int fromTopLeft = (code >>> 2) & 31;
        int d = (code >>> 7) & 3;
        char dir = switch (d) {
            case 0 -> 'u';
            case 1 -> 'd';
            case 2 -> 'l';
            case 3 -> 'r';
            default -> '?';
        };
        return new Move(type, fromTopLeft, dir);
    }

    public static final class Move {
        public final int type;
        public final int fromTopLeft;
        public final char dir;

        public Move(int type, int fromTopLeft, char dir) {
            this.type = type;
            this.fromTopLeft = fromTopLeft;
            this.dir = dir;
        }
    }

    public static final class Node {
        final Board board;
        final long key;
        Node(Board board, long key) {
            this.board = board;
            this.key = key;
        }
    }

    public static final class LongVisitedSet {
        private long[] table;   // stores key+1; 0 = empty
        private long[] parent;
        private int[] move;
        private int size;
        private int resizeAt;

        private static final long ROOT_PARENT = Long.MIN_VALUE;
        private static final int ROOT_MOVE = -1;

        public LongVisitedSet(int expectedSize) {
            int cap = 1;
            while (cap < (expectedSize * 10) / 7) cap <<= 1; // ~0.7 load factor
            table = new long[cap];
            parent = new long[cap];
            move = new int[cap];
            resizeAt = (cap * 7) / 10;
        }

        /** returns true if newly added, false if already present */
        public boolean addIfAbsent(long key, long parentKey, int moveCode) {
            long stored = key + 1;
            if (stored == 0) throw new IllegalArgumentException("Key overflow"); // basically impossible

            if (size >= resizeAt) rehash();

            int mask = table.length - 1;
            int idx = (int) mix64(key) & mask;

            while (true) {
                long cur = table[idx];
                if (cur == 0) {
                    table[idx] = stored;
                    parent[idx] = parentKey;
                    move[idx] = moveCode;
                    size++;
                    return true;
                }
                if (cur == stored) return false;
                idx = (idx + 1) & mask;
            }
        }

        int findSlot(long key) {
            long stored = key + 1;
            int mask = table.length - 1;
            int idx = (int) mix64(key) & mask;

            while (true) {
                long cur = table[idx];
                if (cur == 0) return -1;
                if (cur == stored) return idx;
                idx = (idx + 1) & mask;
            }
        }

        long getParent(long key) {
            int slot = findSlot(key);
            if (slot < 0) throw new IllegalStateException("Key not found");
            return parent[slot];
        }

        int getMoveCode(long key) {
            int slot = findSlot(key);
            if (slot < 0) throw new IllegalStateException("Key not found");
            return move[slot];
        }

        private void rehash() {
            long[] oldTable = table;
            long[] oldParent = parent;
            int[] oldMove = move;

            table = new long[oldTable.length << 1];
            parent = new long[table.length];
            move = new int[table.length];
            size = 0;
            resizeAt = (table.length * 7) / 10;

            int mask = table.length - 1;

            for (int i = 0; i < oldTable.length; i++) {
                long stored = oldTable[i];
                if (stored == 0) continue;

                long key = stored - 1;
                int idx = (int) mix64(key) & mask;
                while (table[idx] != 0) idx = (idx + 1) & mask;

                table[idx] = stored;
                parent[idx] = oldParent[i];
                move[idx] = oldMove[i];
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