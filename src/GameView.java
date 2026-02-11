import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GameView extends JFrame {
    private final Game game;
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;

    public int CELL_SIZE = 100;
    public int BOARD_WIDTH;
    public int BOARD_HEIGHT;

    public int BOARD_LEFT_X;
    public int BOARD_TOP_Y;

    public static final Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
            Color.BLUE, Color.PINK, Color.MAGENTA, Color.CYAN, Color.GRAY, Color.DARK_GRAY,
            Color.getHSBColor((float) (277.0 / 360),1,1), // Purple
            Color.getHSBColor((float) (144.0 / 360), 1f, 0.8f), // Mild Green
            Color.getHSBColor((float) (325.0 / 360), 1f, 1f), // Rose Pink
            Color.getHSBColor((float) (30.0 / 360), 1f, 0.59f), // Brown
            Color.getHSBColor((float) (220.0 / 360), 0.8f, 0.4f)}; // Dark Indigo

    private BufferedImage donkeyPuzzleImage;
    private BufferedImage pennantPuzzleImage;

    public GameView(Game game) {
        // Initial window properties
        this.setTitle("Klotski");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.game = game;

        try {
            donkeyPuzzleImage = ImageIO.read(getClass().getResource("/puzzles/DonkeyPuzzle.png"));
        }
        catch (IOException | NullPointerException ex) {
            donkeyPuzzleImage = null;
        }

        try {
            pennantPuzzleImage = ImageIO.read(getClass().getResource("/puzzles/PennantPuzzle.png"));
        }
        catch (IOException | NullPointerException ex) {
            pennantPuzzleImage = null;
        }

        this.setVisible(true);
    }

    public int getBoardLeftX() {
        return BOARD_LEFT_X;
    }

    public int getBoardTopY() {
        return BOARD_TOP_Y;
    }

    public void clearWindow(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0,0,WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void drawBoard(Graphics g) {
        if (game == null) return;
        if (game.getBoard() == null) return;

        ArrayList<Piece> pieces = game.getBoard().getPieces();
        int nRows = game.getBoard().getNumRows();
        int nCols = game.getBoard().getNumCols();
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                long cellMask = 1L << (nRows * nCols - 1 - (nCols * row + col));
                Color color = Color.WHITE;
                for (int i = 0; i < pieces.size(); i++) {
                    if ((cellMask & pieces.get(i).getLocation()) != 0) {
                        if (i < colors.length) color = colors[i];
                        else color = generateColor(i);
                        break;
                    }
                }
                drawCell(g, BOARD_LEFT_X + CELL_SIZE * col, BOARD_TOP_Y + CELL_SIZE * row, color);
            }
        }
    }

    private static Color generateColor(int i) {
        float hue = 0.05f + (i * 0.61803398875f) % 0.95f;
        float sat = 0.5f + (i * 0.5772156649f) % 0.2f;
        float val = 0.9f + (i * 2.71828183f) % 0.1f;
        return Color.getHSBColor(hue, sat, val);
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
        g.drawString("Press p to pause/play solution", 50, 200);
        g.drawString("Press r to reset board", 50, 250);
        g.drawString("Press esc to return to board selection", 50, 300);
    }

    public void drawSelectingConfig(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawLine(WINDOW_WIDTH / 2, 0, WINDOW_WIDTH / 2, WINDOW_HEIGHT);

        g.drawLine(0, WINDOW_HEIGHT / 2, WINDOW_WIDTH, WINDOW_HEIGHT / 2);

        final int IMG_WIDTH = 200;
        final int IMG_HEIGHT = 250;

        g.drawString("Donkey", 280, 50);
        g.drawString("Pennant", 875, 50);
        g.drawString("5 by 5 random", 280, WINDOW_HEIGHT - 150);
        g.drawString("20 by 20 empty", 875, WINDOW_HEIGHT - 150);

        g.drawImage(donkeyPuzzleImage, 200, 75, IMG_WIDTH, IMG_HEIGHT, null);
        g.drawImage(pennantPuzzleImage, WINDOW_WIDTH - 200 - IMG_WIDTH, 75, IMG_WIDTH, IMG_HEIGHT, null);
    }

    public void paint(Graphics g) {
        clearWindow(g);
        if (game == null) return;
        if (game.selectingConfig) {
            drawSelectingConfig(g);
        }
        else {
            drawBoard(g);
            drawMoveCountText(g);
            drawControlsText(g);
        }
    }
}