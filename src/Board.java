import java.util.ArrayList;

public class Board {
    private int bitboard;
    private ArrayList<Piece> pieces;

    public static final int FIRST_COL_MASK =  0b1000_1000_1000_1000_1000;
    public static final int SECOND_COL_MASK = 0b0100_0100_0100_0100_0100;
    public static final int THIRD_COL_MASK =  0b0010_0010_0010_0010_0010;
    public static final int FOURTH_COL_MASK = 0b0001_0001_0001_0001_0001;
    public static final int[] cols = {FIRST_COL_MASK, SECOND_COL_MASK, THIRD_COL_MASK, FOURTH_COL_MASK};

    public static final int FIRST_ROW_MASK =  0b1111_0000_0000_0000_0000;
    public static final int SECOND_ROW_MASK = 0b0000_1111_0000_0000_0000;
    public static final int THIRD_ROW_MASK =  0b0000_0000_1111_0000_0000;
    public static final int FOURTH_ROW_MASK = 0b0000_0000_0000_1111_0000;
    public static final int FIFTH_ROW_MASK =  0b0000_0000_0000_0000_1111;
    public static final int[] rows = {FIRST_ROW_MASK, SECOND_ROW_MASK, THIRD_ROW_MASK, FOURTH_ROW_MASK, FIFTH_ROW_MASK};


    public Board() {
        bitboard = 0;
        pieces = new ArrayList<Piece>();
        initPieces();
    }

    public void initPieces() {
        addPiece(new Piece(0b0110_0110_0000_0000_0000)); // Big square
        addPiece(new Piece(0b1000_1000_0000_0000_0000)); // Top left vert
        addPiece(new Piece(0b0001_0001_0000_0000_0000)); // Top right vert
        addPiece(new Piece(0b0000_0000_1000_1000_0000)); // Left vert
        addPiece(new Piece(0b0000_0000_0001_0001_0000)); // Right vert
        addPiece(new Piece(0b0000_0000_0110_0000_0000)); // Horizontal
        addPiece(new Piece(0b0000_0000_0000_0100_0000)); // Midleft single
        addPiece(new Piece(0b0000_0000_0000_0010_0000)); // Midright single
        addPiece(new Piece(0b0000_0000_0000_0000_1000)); // Botleft single
        addPiece(new Piece(0b0000_0000_0000_0000_0001)); // Botright single
    }

    public int getBitboard() {
        return bitboard;
    }

    public void setBoard(int bitboard) {
        this.bitboard = bitboard;
    }

    public void addPiece(Piece piece) {
        int mask = piece.getLocation();
        assert ((bitboard & mask) == 0) : "Attempting to add piece to occupied location";
        pieces.add(piece);
        bitboard |= mask;
    }

    public void removePiece(Piece piece) {
        pieces.remove(piece);
        bitboard &= ~piece.getLocation();
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public boolean movePiece(char pieceChar, char dir) {
        int pieceIdx = pieceChar - 'a';
        Piece piece = pieces.get(pieceIdx);

        // Input validation
        if (pieceIdx < 0 || pieceIdx >= pieces.size()) return false;
        if (dir != 'u' && dir != 'd' && dir != 'l' && dir != 'r') return false;

        // Don't move outside board or wrap around
        if (dir == 'u' && piece.touchingTop()) return false;
        if (dir == 'd' && piece.touchingBottom()) return false;
        if (dir == 'l' && piece.touchingLeft()) return false;
        if (dir == 'r' && piece.touchingRight()) return false;

        int oldMask = piece.getLocation();
        int newMask;

        if (dir == 'u') newMask = oldMask << 4;
        else if (dir == 'd') newMask = oldMask >> 4;
        else if (dir == 'l') newMask = oldMask << 1;
        else newMask = oldMask >> 1;


        // If the position would be taken, return false
        int otherPiecesBoard = bitboard & ~oldMask;
        if ((newMask & otherPiecesBoard) != 0) return false;

        piece.move(newMask);
        syncBitboard();
        return true;
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
        int newMask = 0;

        if (dir == 'u') newMask = oldMask << 4;
        else if (dir == 'd') newMask = oldMask >> 4;
        else if (dir == 'l') newMask = oldMask << 1;
        else newMask = oldMask >> 1;


        // If the position would be taken, return false
        int otherPiecesBoard = bitboard & ~oldMask;
        if ((newMask & otherPiecesBoard) != 0) return false;

        piece.move(newMask);
        syncBitboard();
        return true;
    }

    public boolean occupied(int mask) {
        return ((bitboard & mask) != 0);
    }

    public void syncBitboard() {
        bitboard = 0;
        for (Piece piece : pieces) {
            bitboard |= piece.getLocation();
        }
    }

    public void printBoard() {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 4; c++) {
                int bitIndex = 19 - (r * 4 + c);
                int squareMask = 1 << bitIndex;

                char symbol = '·'; // default: empty

                if ((bitboard & squareMask) != 0) {
                    for (int i = 0; i < pieces.size(); i++) {
                        if ((pieces.get(i).getLocation() & squareMask) != 0) {
                            symbol = (char) ('a' + i);
                            break;
                        }
                    }
                }

                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }


}
