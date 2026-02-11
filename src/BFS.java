import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BFS {
    private final Visited visited;
    private final int goalSquare;
    private static final char[] DIRS = {'u','d','l','r'};
    private static final int ROOT_PARENT = -1;
    private static final int ROOT_MOVE = -1;

    private final int a22Count;
    private final int a21Count;
    private final int a12Count;
    private final int a11Count;

    public BFS(Board board) {
        visited = new Visited(200_000);
        this.goalSquare = board.getGoalSquare();

        int a22Count = 0;
        int a21Count = 0;
        int a12Count = 0;
        int a11Count = 0;
        for (Piece piece : board.getPieces()) {
            switch (piece.getType()) {
                case Piece.TWO_BY_TWO:
                    a22Count++;
                    break;
                case Piece.TWO_BY_ONE:
                    a21Count++;
                    break;
                case Piece.ONE_BY_TWO:
                    a12Count++;
                    break;
                case Piece.ONE_BY_ONE:
                    a11Count++;
                    break;
            }
        }
        this.a22Count = a22Count;
        this.a21Count = a21Count;
        this.a12Count = a12Count;
        this.a11Count = a11Count;
    }

    private boolean isGoal(Board board) {
        for (Piece p : board.getPieces()) {
            if (p.getType() == Piece.TWO_BY_TWO) {
                return p.getTopLeft() == goalSquare;
            }
        }
        throw new IllegalStateException("No 2x2 piece found.");
    }

    public ArrayList<Move> solve(Board start) {
        ArrayDeque<Node> q = new ArrayDeque<>();

        BoardState startState = new BoardState(start, a22Count, a21Count, a12Count, a11Count);
        int startId = visited.addIfAbsent(startState, ROOT_PARENT, ROOT_MOVE);

        q.addLast(new Node(start, startId));

        int numCombsChecked = 0;
        while (!q.isEmpty()) {
            numCombsChecked++;
            if (numCombsChecked % 50_000 == 0) {
                System.out.println("Num combs checked: " + (numCombsChecked / 1000) + ",000");
                System.out.println("Current visited size: " + visited.size);
                System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
                System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
                System.out.println("Max memory: " + Runtime.getRuntime().maxMemory());
                System.out.println("~~~~~~~~~~~~");
            }
            Node cur = q.removeFirst();
            if (isGoal(cur.board)) return reconstructMoves(cur.id);
            ArrayList<Piece> curPieces = cur.board.getPieces();
            for (int i = 0; i < curPieces.size(); i++) {
                Piece p = curPieces.get(i);
                int type = p.getType();
                int fromTopLeft = p.getTopLeft();
                for (char dir : DIRS) {
                    Board next = new Board(cur.board);
                    Piece moved = next.getPieces().get(i);
                    if (!next.movePiece(moved, dir)) continue;
                    BoardState nextState = new BoardState(next, a22Count, a21Count, a12Count, a11Count);
                    int moveCode = packMove(type, fromTopLeft, dir);
                    int nextId = visited.addIfAbsent(nextState, cur.id, moveCode);
                    if (nextId >= 0) {
                        q.addLast(new Node(next, nextId));
                    }
                }
            }
        }
        return null; // no solution
    }

    private ArrayList<Move> reconstructMoves(int goalId) {
        ArrayList<Move> reversed = new ArrayList<Move>();
        int id = goalId;
        while (id != ROOT_PARENT) {
            int code = visited.getMoveCodeById(id);
            if (code == ROOT_MOVE) break;
            reversed.add(unpackMove(code));
            id = visited.getParentById(id);
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
        return (type & 3) | ((d & 3) << 2) | (fromTopLeft << 4);
    }

    private static Move unpackMove(int code) {
        int type = code & 3;
        int d = (code >>> 2) & 3;
        int fromTopLeft = code >>> 4;
        char dir = switch (d) {
            case 0 -> 'u';
            case 1 -> 'd';
            case 2 -> 'l';
            case 3 -> 'r';
            default -> '?';
        };
        return new Move(type, fromTopLeft, dir);
    }

    public static final class BoardState {
        private final int[] a22;
        private final int[] a21;
        private final int[] a12;
        private final int[] a11;

        BoardState(Board board, int a22Count, int a21Count, int a12Count, int a11Count) {
            int[] t22 = new int[a22Count];
            int[] t21 = new int[a21Count];
            int[] t12 = new int[a12Count];
            int[] t11 = new int[a11Count];

            int i22 = 0;
            int i21 = 0;
            int i12 = 0;
            int i11 = 0;
            for (Piece piece : board.getPieces()) {
                int topLeft = piece.getTopLeft();
                switch (piece.getType()) {
                    case Piece.TWO_BY_TWO -> t22[i22++] = topLeft;
                    case Piece.TWO_BY_ONE -> t21[i21++] = topLeft;
                    case Piece.ONE_BY_TWO -> t12[i12++] = topLeft;
                    case Piece.ONE_BY_ONE -> t11[i11++] = topLeft;
                    default -> throw new IllegalStateException("Unknown piece type");
                }
            }

            Arrays.sort(t22);
            Arrays.sort(t21);
            Arrays.sort(t12);
            Arrays.sort(t11);

            this.a22 = t22;
            this.a21 = t21;
            this.a12 = t12;
            this.a11 = t11;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BoardState other)) return false;
            return (Arrays.equals(a22, other.a22)
                    && Arrays.equals(a21, other.a21)
                    && Arrays.equals(a12, other.a12)
                    && Arrays.equals(a11, other.a11));
        }

        @Override
        public int hashCode() {
            int h = 1;
            h = 31 * h + Arrays.hashCode(a22);
            h = 31 * h + Arrays.hashCode(a21);
            h = 31 * h + Arrays.hashCode(a12);
            h = 31 * h + Arrays.hashCode(a11);
            return h;
        }
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
        final int id;

        Node(Board board, int id) {
            this.board = board;
            this.id = id;
        }
    }

    public static final class Visited {
        private BoardState[] keys;
        private int[] idAtSlot;
        private int[] parentById;    // indexed by stable id
        private int[] moveById;      // indexed by stable id
        private int nextId;
        private int size;
        private int resizeAt;

        public Visited(int expectedSize) {
            int cap = 1;
            while (cap < (expectedSize * 10) / 7) cap <<= 1; // Load factor 0.7
            keys = new BoardState[cap];
            idAtSlot = new int[cap];
            resizeAt = (cap * 7) / 10;

            int idCap = Math.max(16, expectedSize + 1);
            parentById = new int[idCap];
            moveById = new int[idCap];

            nextId = 1;
        }

        public int addIfAbsent(BoardState key, int parentSlot, int moveCode) {
            if (size >= resizeAt) rehash();

            int mask = keys.length - 1;
            int idx = mix32(key.hashCode()) & mask;

            while (true) {
                BoardState cur = keys[idx];
                if (cur == null) {
                    int id = nextId++;
                    ensureIdCapacity(id);

                    keys[idx] = key;
                    idAtSlot[idx] = id;

                    parentById[id] = parentSlot;
                    moveById[id] = moveCode;

                    size++;
                    return id;
                }
                if (cur.equals(key)) {
                    return -1;
                }
                idx = (idx + 1) & mask;
            }
        }

        public int getParentById(int id) {
            return parentById[id];
        }

        public int getMoveCodeById(int id) {
            return moveById[id];
        }

        private void ensureIdCapacity(int id) {
            if (id < parentById.length) return;

            int newCap = parentById.length;
            while (newCap <= id) newCap <<= 1;

            parentById = Arrays.copyOf(parentById, newCap);
            moveById = Arrays.copyOf(moveById, newCap);
        }

        private void rehash() {
            BoardState[] oldKeys = keys;
            int[] oldIdAtSlot = idAtSlot;

            int newCap = oldKeys.length << 1;
            keys = new BoardState[newCap];
            idAtSlot = new int[newCap];

            size = 0;
            resizeAt = (keys.length * 7) / 10;
            int mask = newCap - 1;

            for (int i = 0; i < oldKeys.length; i++) {
                BoardState k = oldKeys[i];
                if (k == null) continue;

                int id = oldIdAtSlot[i];

                int idx = mix32(k.hashCode()) & mask;
                while (keys[idx] != null) idx = (idx + 1) & mask;

                keys[idx] = k;
                idAtSlot[idx] = id;
                size++;
            }
        }

        private static int mix32(int h) {
            h ^= (h >>> 16);
            h *= 0x7feb352d;
            h ^= (h >>> 15);
            h *= 0x846ca68b;
            h ^= (h >>> 16);
            return h;
        }
    }
}