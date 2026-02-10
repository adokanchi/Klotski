public class Piece {
    private long bitboard;
    private final int type;
    private int topLeft;
    private final Board board;

    public static final int TWO_BY_TWO = 0;
    public static final int TWO_BY_ONE = 1;
    public static final int ONE_BY_TWO = 2;
    public static final int ONE_BY_ONE = 3;

    public Piece(int topLeft, int height, int width, Board board) {
        int nCols = board.getNumCols();
        int nRows = board.getNumRows();
        int row  = (nRows - 1) - topLeft / nCols;
        int col = (nCols - 1) - topLeft % nCols;
        int N = nCols * nRows;

        if (topLeft < 0 || topLeft >= N) {
            throw new IllegalArgumentException("topLeft out of range: " + topLeft);
        }
        if (col + width > nCols) {
            throw new IllegalArgumentException("Constructed Piece overflows right edge");
        }
        if (row + height > nRows) {
            throw new IllegalArgumentException("Constructed Piece overflows bottom edge");
        }

        this.topLeft = topLeft;
        this.board = board;
        bitboard = 0L;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int cell = topLeft - r * nCols - c;
                bitboard |= (1L << cell);
            }
        }

        if (width == 2 && height == 2) type = TWO_BY_TWO;
        else if (width == 1 && height == 2) type = TWO_BY_ONE;
        else if (width == 2 && height == 1) type = ONE_BY_TWO;
        else if (width == 1 && height == 1) type = ONE_BY_ONE;
        else throw new IllegalArgumentException("Unsupported size: " + height + "x" + width);
    }

    public Piece(Piece other) {
        bitboard = other.getLocation();
        type = other.getType();
        topLeft = other.getTopLeft();
        board = other.getBoard();
    }

    public long getLocation() {
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

    public Board getBoard() {
        return board;
    }

    public void move(long bitboard) {
        this.bitboard = bitboard;
    }
}