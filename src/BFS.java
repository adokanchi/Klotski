public class BFS {
    private boolean[] explored;
    private final int CAPACITY = 1;

    public BFS() {
        explored = new boolean[CAPACITY];
    }

    public boolean explored(int i) {
        return explored[i];
    }

    public boolean explored(Board board) {
        // TODO
        return false;
    }

    public boolean explored(Game game) {
        // TODO
        return false;
    }

    public static void main(String[] args) {

    }
}
