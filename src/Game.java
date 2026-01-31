import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;

public class Game implements MouseListener, KeyListener, ActionListener {
    private GameView window;
    private Board board;

    public Board getBoard() {
        return board;
    }

    public void runGame() {
        window = new GameView(this);
        this.window.addMouseListener(this);
        this.window.addKeyListener(this);
        Toolkit.getDefaultToolkit().sync();

        runGameLoop();
    }

    public void runGameLoop() {
        board = new Board();
        Scanner input = new Scanner(System.in);
        int moveCount = 0;
        while (true) {
            for (int i = 0; i < 10; i++) System.out.println(); // Clear screen
            System.out.println("Moves: " + moveCount);
            board.printBoard();
            window.repaint();
            System.out.print("Piece to move: ");
            String in = input.nextLine();
            char pieceChar = in.isEmpty() ? ' ' : in.charAt(0);

            System.out.println("Direction to move: (u/d/l/r): ");
            in = input.nextLine();
            char dir = in.isEmpty() ? 'u' : in.charAt(0);

            // Move piece
            if (board.movePiece(pieceChar, dir)) moveCount++;
        }
    }

    public void mouseClicked(MouseEvent e) {}
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