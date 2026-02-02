public class Piece {
    private int bitboard;
    private int type;
    private int topLeft;

    public static final int TWO_BY_TWO = 0;
    public static final int TWO_BY_ONE = 1;
    public static final int ONE_BY_TWO = 2;
    public static final int ONE_BY_ONE = 3;

    public Piece(int bitboard, int type) {
        this.bitboard = bitboard;
        this.type = type;
        for (int i = 19; i >= 0; i--) {
            if (((bitboard >> i) & 1) != 0) {
                topLeft = i;
                break;
            }
        }
    }

    public Piece(Piece other) {
        bitboard = other.getLocation();
        type = other.getType();
        topLeft = other.getTopLeft();
    }

    public int getLocation() {
        return bitboard;
    }

    public int getType() {
        return type;
    }

    public int getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(int topLeft) {
        this.topLeft = topLeft;
    }

    public void setTopLeft() {
        for (int i = 19; i >= 0; i--) {
            if (((bitboard >> i) & 1) != 0) {
                topLeft = i;
                break;
            }
        }
    }

    public void move(int bitboard) {
        this.bitboard = bitboard;
    }

    public boolean touchingLeft() {
        return ((bitboard & Board.FIRST_COL_MASK) != 0);
    }

    public boolean touchingRight() {
        return ((bitboard & Board.FOURTH_COL_MASK) != 0);
    }

    public boolean touchingTop() {
        return ((bitboard & Board.FIRST_ROW_MASK) != 0);
    }

    public boolean touchingBottom() {
        return ((bitboard & Board.FIFTH_ROW_MASK) != 0);
    }

}