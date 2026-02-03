import java.util.ArrayList;

public class Board {
    private int bitboard;
    private final ArrayList<Piece> pieces;

    public static final int FIRST_COL_MASK =  0b1000_1000_1000_1000_1000;
    public static final int FOURTH_COL_MASK = 0b0001_0001_0001_0001_0001;

    public static final int FIRST_ROW_MASK =  0b1111_0000_0000_0000_0000;
    public static final int FIFTH_ROW_MASK =  0b0000_0000_0000_0000_1111;

    public Board() {
        bitboard = 0;
        pieces = new ArrayList<>();
        initPiecesDonkey();
    }

    public Board(Board other) {
        bitboard = other.getBitboard();
        pieces = new ArrayList<>();
        for (Piece piece : other.getPieces()) {
            pieces.add(new Piece(piece));
        }
    }

    private void clear() {
        pieces.clear();
        bitboard = 0;
    }

    public void initPiecesDonkey() {
        clear();
        addPiece(new Piece(0b0110_0110_0000_0000_0000, Piece.TWO_BY_TWO)); // Big square

        addPiece(new Piece(0b1000_1000_0000_0000_0000, Piece.TWO_BY_ONE)); // Top left vert
        addPiece(new Piece(0b0001_0001_0000_0000_0000, Piece.TWO_BY_ONE)); // Top right vert
        addPiece(new Piece(0b0000_0000_1000_1000_0000, Piece.TWO_BY_ONE)); // Left vert
        addPiece(new Piece(0b0000_0000_0001_0001_0000, Piece.TWO_BY_ONE)); // Right vert

        addPiece(new Piece(0b0000_0000_0110_0000_0000, Piece.ONE_BY_TWO)); // Horizontal

        addPiece(new Piece(0b0000_0000_0000_0100_0000, Piece.ONE_BY_ONE)); // Midleft single
        addPiece(new Piece(0b0000_0000_0000_0010_0000, Piece.ONE_BY_ONE)); // Midright single
        addPiece(new Piece(0b0000_0000_0000_0000_1000, Piece.ONE_BY_ONE)); // Botleft single
        addPiece(new Piece(0b0000_0000_0000_0000_0001, Piece.ONE_BY_ONE)); // Botright single
    }

    public void initPiecesPennant() {
        clear();
        addPiece(new Piece(0b1100_1100_0000_0000_0000, Piece.TWO_BY_TWO)); // Big square

        addPiece(new Piece(0b0000_0000_0000_1000_1000, Piece.TWO_BY_ONE));
        addPiece(new Piece(0b0000_0000_0000_0100_0100, Piece.TWO_BY_ONE));

        addPiece(new Piece(0b0011_0000_0000_0000_0000, Piece.ONE_BY_TWO));
        addPiece(new Piece(0b0000_0011_0000_0000_0000, Piece.ONE_BY_TWO));
        addPiece(new Piece(0b0000_0000_0000_0011_0000, Piece.ONE_BY_TWO));
        addPiece(new Piece(0b0000_0000_0000_0000_0011, Piece.ONE_BY_TWO));

        addPiece(new Piece(0b0000_0000_1000_0000_0000, Piece.ONE_BY_ONE));
        addPiece(new Piece(0b0000_0000_0100_0000_0000, Piece.ONE_BY_ONE));
    }

    public int getBitboard() {
        return bitboard;
    }

    public void addPiece(Piece piece) {
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
        if (dir == 'u' && piece.touchingTop()) return false;
        if (dir == 'd' && piece.touchingBottom()) return false;
        if (dir == 'l' && piece.touchingLeft()) return false;
        if (dir == 'r' && piece.touchingRight()) return false;

        int oldMask = piece.getLocation();
        int oldTopLeft = piece.getTopLeft();
        int newMask;
        int newTopLeft;

        if (dir == 'u') {
            newMask = oldMask << 4;
            newTopLeft = oldTopLeft + 4;
        }
        else if (dir == 'd') {
            newMask = oldMask >> 4;
            newTopLeft = oldTopLeft - 4;
        }
        else if (dir == 'l') {
            newMask = oldMask << 1;
            newTopLeft = oldTopLeft + 1;
        }
        else {
            newMask = oldMask >> 1;
            newTopLeft = oldTopLeft - 1;
        }

        // If the position would be taken, return false
        int otherPiecesBoard = bitboard & ~oldMask;
        if ((newMask & otherPiecesBoard) != 0) return false;

        piece.move(newMask);
        piece.setTopLeft(newTopLeft);
        syncBitboard();
        return true;
    }

    public void syncBitboard() {
        bitboard = 0;
        for (Piece piece : pieces) {
            bitboard |= piece.getLocation();
        }
    }

}