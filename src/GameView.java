import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameView extends JFrame {
    private final Game game;
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;

    public static final int CELL_SIZE = 100;
    public static final int BOARD_WIDTH = 4 * CELL_SIZE;
    public static final int BOARD_HEIGHT = 5 * CELL_SIZE;

    public static final int BOARD_LEFT_X = (WINDOW_WIDTH - BOARD_WIDTH) / 2;

    public static final int BOARD_TOP_Y = (WINDOW_HEIGHT - BOARD_HEIGHT) / 2;

    public static final Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW,
                                          Color.GREEN, Color.BLUE, Color.PINK,
                                          Color.MAGENTA, Color.CYAN, Color.GRAY, Color.DARK_GRAY};

    public GameView(Game game) {
        // Initial window properties
        this.setTitle("Klotski");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setVisible(true);
        this.game = game;
    }

    public void clearWindow(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0,0,WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void drawBoard(Graphics g) {
        if (game == null) return;
        if (game.getBoard() == null) return;

        ArrayList<Piece> pieces = game.getBoard().getPieces();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                int cellMask = 1 << (19 - (4 * row + col));
                Color color = Color.WHITE;
                for (int i = 0; i < pieces.size(); i++) {
                    if ((cellMask & pieces.get(i).getLocation()) != 0) {
                        color = colors[i];
                        break;
                    }
                }
                drawCell(g, BOARD_LEFT_X + CELL_SIZE * col, BOARD_TOP_Y + CELL_SIZE * row, color);
            }
        }
    }

    public void drawCell(Graphics g, int topleftX, int topleftY, Color color) {
        int OUTLINE_WIDTH = 3;
        // Outline
        g.setColor(Color.BLACK);
        g.fillRect(topleftX, topleftY, CELL_SIZE, CELL_SIZE);

        // Center
        g.setColor(color);
        g.fillRect(topleftX + OUTLINE_WIDTH, topleftY + OUTLINE_WIDTH, CELL_SIZE - 2 * OUTLINE_WIDTH, CELL_SIZE - 2 * OUTLINE_WIDTH);
    }

    public void drawMoveCountText(Graphics g) {
        g.setColor(Color.BLACK);
        String moveCountText = "Move Count: " + game.getMoveCount();
        g.drawString(moveCountText, 50, 100);
    }

    public void drawControlsText(Graphics g) {
        g.drawString("Press p to pause/play solution", 50, 150);
        g.drawString("Press r to reset", 50, 200);
    }

    public void paint(Graphics g) {
        clearWindow(g);
        drawBoard(g);
        drawMoveCountText(g);
        drawControlsText(g);
    }
}