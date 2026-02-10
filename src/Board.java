import java.util.ArrayList;

public class Board {
    private long bitboard;
    private final ArrayList<Piece> pieces;

    public int NUM_ROWS;
    public int NUM_COLS;

    public long FIRST_COL_MASK;
    public long LAST_COL_MASK;

    public long FIRST_ROW_MASK;
    public long LAST_ROW_MASK;

    private int goalSquare;

    public Board() {
        bitboard = 0;
        NUM_ROWS = 5;
        NUM_COLS = 4;
        FIRST_COL_MASK = 0b1000_1000_1000_1000_1000;
        LAST_COL_MASK  = 0b0001_0001_0001_0001_0001;
        FIRST_ROW_MASK = 0b1111_0000_0000_0000_0000;
        LAST_ROW_MASK  = 0b0000_0000_0000_0000_1111;
        pieces = new ArrayList<>();
    }

    public Board(int rows, int cols) {
        bitboard = 0L;
        NUM_ROWS = rows;
        NUM_COLS = cols;
        FIRST_COL_MASK = 0L;
        LAST_COL_MASK = 0L;
        for (int i = 0; i < NUM_ROWS; i++) {
            FIRST_COL_MASK <<= NUM_COLS;
            FIRST_COL_MASK |= 1L << (NUM_COLS - 1);
            LAST_COL_MASK <<= NUM_COLS;
            LAST_COL_MASK |= 1L;
        }
        FIRST_ROW_MASK = ((1L << NUM_COLS) - 1) << ((NUM_ROWS - 1) * NUM_COLS);
        LAST_ROW_MASK = (1L << NUM_COLS) - 1;

        pieces = new ArrayList<>();
    }

    public Board(Board other) {
        bitboard = other.getBitboard();
        NUM_ROWS = other.getNumRows();
        NUM_COLS = other.getNumCols();
        pieces = new ArrayList<>();
        for (Piece piece : other.getPieces()) {
            pieces.add(new Piece(piece));
        }
        FIRST_COL_MASK = other.FIRST_COL_MASK;
        LAST_COL_MASK = other.LAST_COL_MASK;
        FIRST_ROW_MASK = other.FIRST_ROW_MASK;
        LAST_ROW_MASK = other.LAST_ROW_MASK;
        goalSquare = other.goalSquare;
    }

    private void clear() {
        pieces.clear();
        bitboard = 0;
    }

    public void initPiecesDonkey() {
        clear();

        addPiece(new Piece(18, 2, 2, this)); // Big square

        addPiece(new Piece(19, 2, 1, this)); // Top left vert
        addPiece(new Piece(16, 2, 1, this)); // Top right vert
        addPiece(new Piece(11, 2, 1, this)); // Left vert
        addPiece(new Piece(8, 2, 1, this)); // Right vert

        addPiece(new Piece(10, 1, 2, this)); // Horizontal

        addPiece(new Piece(6, 1, 1, this)); // Midleft single
        addPiece(new Piece(5, 1, 1, this)); // Midright single
        addPiece(new Piece(3, 1, 1, this)); // Botleft single
        addPiece(new Piece(0, 1, 1, this)); // Botright single

        goalSquare = 6;
    }

    public void initPiecesPennant() {
        clear();
        addPiece(new Piece(19, 2, 2, this)); // Big square

        addPiece(new Piece(7, 2, 1, this));
        addPiece(new Piece(6, 2, 1, this));

        addPiece(new Piece(17, 1, 2, this));
        addPiece(new Piece(13, 1, 2, this));
        addPiece(new Piece(5, 1, 2, this));
        addPiece(new Piece(1, 1, 2, this));

        addPiece(new Piece(11, 1, 1, this));
        addPiece(new Piece(10, 1, 1, this));

        goalSquare = 7;
    }

    public long getBitboard() {
        return bitboard;
    }

    public int getNumRows() {
        return NUM_ROWS;
    }

    public int getNumCols() {
        return NUM_COLS;
    }

    public int getGoalSquare() {
        return goalSquare;
    }

    public void addPiece(Piece piece) {
        if ((piece.getLocation() & bitboard) != 0) throw new IllegalArgumentException("New piece overlaps previous piece.");
        pieces.add(piece);
        bitboard |= piece.getLocation();
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public boolean movePiece(Piece piece, char dir) {
        // Input validation
        if (!pieces.contains(piece)) return false;
        if (dir != 'u' && dir != 'd' && dir != 'l' && dir != 'r') return false;

        // Don't move outside board or wrap around
        if (dir == 'u' && isTouchingTop(piece)) return false;
        if (dir == 'd' && isTouchingBottom(piece)) return false;
        if (dir == 'l' && isTouchingLeft(piece)) return false;
        if (dir == 'r' && isTouchingRight(piece)) return false;

        long oldMask = piece.getLocation();
        int oldTopLeft = piece.getTopLeft();
        long newMask;
        int newTopLeft;

        if (dir == 'u') {
            newMask = oldMask << NUM_COLS;
            newTopLeft = oldTopLeft + NUM_COLS;
        }
        else if (dir == 'd') {
            newMask = oldMask >>> NUM_COLS;
            newTopLeft = oldTopLeft - NUM_COLS;
        }
        else if (dir == 'l') {
            newMask = oldMask << 1;
            newTopLeft = oldTopLeft + 1;
        }
        else {
            newMask = oldMask >>> 1;
            newTopLeft = oldTopLeft - 1;
        }

        // If the position would be taken, return false
        long otherPiecesBoard = bitboard & ~oldMask;
        if ((newMask & otherPiecesBoard) != 0) return false;

        piece.move(newMask);
        piece.setTopLeft(newTopLeft);
        syncBitboard();
        return true;
    }

    public boolean isTouchingLeft(Piece piece) {
        return ((piece.getLocation() & FIRST_COL_MASK) != 0);
    }

    public boolean isTouchingRight(Piece piece) {
        return ((piece.getLocation() & LAST_COL_MASK) != 0);
    }

    public boolean isTouchingTop(Piece piece) {
        return ((piece.getLocation() & FIRST_ROW_MASK) != 0);
    }

    public boolean isTouchingBottom(Piece piece) {
        return ((piece.getLocation() & LAST_ROW_MASK) != 0);
    }

    public void syncBitboard() {
        bitboard = 0;
        for (Piece piece : pieces) {
            bitboard |= piece.getLocation();
        }
    }
}