public class Piece {
    private int bitboard;

    public Piece() {
        bitboard = 0;
    }

    public Piece(int bitboard) {
        this.bitboard = bitboard;
    }

    public int getLocation() {
        return bitboard;
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
