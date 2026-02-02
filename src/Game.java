import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;

public class Game implements MouseListener, KeyListener, ActionListener {
    private GameView window;
    private Board board;
    private Piece selectedPiece;
    private int moveCount;

    public Game() {
        board = new Board();
        moveCount = 0;
    }

    public Board getBoard() {
        return board;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void runGame() {
        window = new GameView(this);
        this.window.addMouseListener(this);
        this.window.addKeyListener(this);
        Toolkit.getDefaultToolkit().sync();
    }

    public void mouseClicked(MouseEvent e) {
        if (board == null) return;
        if (window == null) return;

        int x = e.getX();
        int y = e.getY();

        int row = (y - GameView.BOARD_TOP_Y) / GameView.CELL_SIZE;
        int col = (x - GameView.BOARD_LEFT_X) / GameView.CELL_SIZE;

        if (row < 0 || col < 0 || row > 5 || col > 4) selectedPiece = null;

        int cellNum = 19 - (row * 4 + col);
        int cellMask = 1 << cellNum;
        if ((cellMask & board.getBitboard()) != 0) { // Clicked a piece
            Piece clickedPiece = null;
            for (Piece piece : board.getPieces()) {
                if ((cellMask & piece.getLocation()) != 0) {
                    clickedPiece = piece;
                    break;
                }
            }
//            Uncomment to allow clicking a selected piece to deselect it
//            if (selectedPiece == clickedPiece) {
//                selectedPiece = null;
//            }
//            else {
                selectedPiece = clickedPiece;
//            }
        }
        else { // Clicked empty space
            // If nothing selected, do nothing
            if (selectedPiece == null) return;

            // Otherwise, attempt to move a piece or deselect
            char dir;
            if (!selectedPiece.touchingLeft() && ((cellMask >> 1) & selectedPiece.getLocation()) != 0) {
                dir = 'l';
            }
            else if (!selectedPiece.touchingRight() && ((cellMask << 1) & selectedPiece.getLocation()) != 0) {
                dir = 'r';
            }
            else if (!selectedPiece.touchingTop() && ((cellMask >> 4) & selectedPiece.getLocation()) != 0) {
                dir = 'u';
            }
            else if (!selectedPiece.touchingBottom() && ((cellMask << 4) & selectedPiece.getLocation()) != 0) {
                dir = 'd';
            }
            else {
                selectedPiece = null;
                return;
            }

            if (board.movePiece(selectedPiece, dir)) {
                moveCount++;
            }
        }

        window.repaint();
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {}

    // Timer clock = new Timer(500, this);
    public void actionPerformed(ActionEvent e) {}

    public static void main(String[] args) {
        Game game = new Game();
        game.runGame();
    }
}