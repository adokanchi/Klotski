import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.ArrayList;

public class Game implements MouseListener, KeyListener, ActionListener {
    private GameView window;
    private Board board;
    private Piece selectedPiece;
    private int moveCount;
    private Timer clock;
    private ArrayList<BFS.Move> solution;
    private int solutionStep = 0;
    public boolean selectingConfig;
    private boolean isCurrPuzzleDonkey;

    public Game() {
        board = new Board();
        moveCount = 0;
        selectingConfig = true;
    }

    public Board getBoard() {
        return board;
    }

    public int getMoveCount() {
        return moveCount;
    }

    private boolean applyMove(Board board, BFS.Move m) {
        for (Piece p : board.getPieces()) {
            if (p.getType() == m.type && p.getTopLeft() == m.fromTopLeft) {
                return board.movePiece(p, m.dir);
            }
        }
        return false;
    }

    public void runGame() {
        window = new GameView(this);
        this.window.addMouseListener(this);
        this.window.addKeyListener(this);
        final int ANIM_DELAY = 333;
        clock = new Timer(ANIM_DELAY, this);
        Toolkit.getDefaultToolkit().sync();
    }

    private void toggleAutoplay() {
        if (clock.isRunning()) {
            clock.stop();
        }
        else {
            BFS bfs = new BFS(board);
            solution = bfs.solve(new Board(board));
            if (solution == null) {
                System.out.println("No solution found.");
                return;
            }
            solutionStep = 0;
            clock.start();
        }
    }

    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (selectingConfig) {
            if (x < GameView.WINDOW_WIDTH / 2 && y < GameView.WINDOW_HEIGHT / 2) {
                board = new Board();
                board.initPiecesDonkey();
                isCurrPuzzleDonkey = true;
            }
            else if (x >= GameView.WINDOW_WIDTH / 2 && y < GameView.WINDOW_HEIGHT / 2) {
                board = new Board();
                board.initPiecesPennant();
                isCurrPuzzleDonkey = false;
            }
            else if (x < GameView.WINDOW_WIDTH / 2 && y >= GameView.WINDOW_HEIGHT / 2) {
                board = new Board(6, 6);
            }
            else if (x >= GameView.WINDOW_WIDTH / 2 && y >= GameView.WINDOW_HEIGHT / 2) {
                board = new Board(20, 20);
            }

            int nRows = board.getNumRows();
            int nCols = board.getNumCols();

            if (nRows <= 6 && nCols <= 6) {
                window.CELL_SIZE = 100;
            }
            else {
                window.CELL_SIZE = 600 / Math.max(nRows, nCols);
            }

            window.BOARD_WIDTH = nCols * window.CELL_SIZE;
            window.BOARD_HEIGHT = nRows * window.CELL_SIZE;

            window.BOARD_LEFT_X = (GameView.WINDOW_WIDTH - window.BOARD_WIDTH) / 2;
            window.BOARD_TOP_Y = (GameView.WINDOW_HEIGHT - window.BOARD_HEIGHT) / 2;

            selectingConfig = false;
            window.repaint();
            return;
        }

        if (board == null) return;
        if (window == null) return;

        if (clock.isRunning()) return;

        int row = (y - window.getBoardTopY()) / window.CELL_SIZE;
        int col = (x - window.getBoardLeftX()) / window.CELL_SIZE;

        if (row < 0 || col < 0 || row >= board.getNumRows() || col >= board.getNumCols()) {
            selectedPiece = null;
            window.repaint();
            return;
        }

        int cellNum = board.getNumRows() * board.getNumCols() - 1 - (row * board.getNumCols() + col);
        long cellMask = 1L << cellNum;
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
            if (!board.isTouchingLeft(selectedPiece) && ((cellMask >>> 1) & selectedPiece.getLocation()) != 0) {
                dir = 'l';
            }
            else if (!board.isTouchingRight(selectedPiece) && ((cellMask << 1) & selectedPiece.getLocation()) != 0) {
                dir = 'r';
            }
            else if (!board.isTouchingTop(selectedPiece) && ((cellMask >>> board.getNumCols()) & selectedPiece.getLocation()) != 0) {
                dir = 'u';
            }
            else if (!board.isTouchingBottom(selectedPiece) && ((cellMask << board.getNumCols()) & selectedPiece.getLocation()) != 0) {
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
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            toggleAutoplay();
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            if (isCurrPuzzleDonkey) {
                board.initPiecesDonkey();
            }
            else {
                board.initPiecesPennant();
            }
            moveCount = 0;
            window.repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            clock.stop();
            moveCount = 0;
            selectingConfig = true;
            window.repaint();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (board == null) return;
        if (solution == null) return;

        if (solutionStep >= solution.size()) {
            clock.stop();
            return;
        }

        BFS.Move m = solution.get(solutionStep);

        boolean applied = applyMove(board, m);
        if (!applied) {
            clock.stop();
            throw new IllegalStateException("Failed to apply move at step " + solutionStep + ": " + m);
        }

        solutionStep++;
        moveCount++;
        window.repaint();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.runGame();
    }
}