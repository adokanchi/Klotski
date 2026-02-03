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
            BFS bfs = new BFS(200_000, (isCurrPuzzleDonkey ? 6 : 7));
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
        if (selectingConfig) {
            if (e.getX() < GameView.WINDOW_WIDTH / 2) {
                board.initPiecesDonkey();
                isCurrPuzzleDonkey = true;
            }
            else {
                board.initPiecesPennant();
                isCurrPuzzleDonkey = false;
            }
            selectingConfig = false;
            window.repaint();
            return;
        }


        if (board == null) return;
        if (window == null) return;

        if (clock.isRunning()) return;

        int x = e.getX();
        int y = e.getY();

        int row = (y - GameView.BOARD_TOP_Y) / GameView.CELL_SIZE;
        int col = (x - GameView.BOARD_LEFT_X) / GameView.CELL_SIZE;

        if (row < 0 || col < 0 || row >= 5 || col >= 4) {
            selectedPiece = null;
            window.repaint();
            return;
        }

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
            board = new Board();
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
            System.out.println("Finished autoplay.");
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