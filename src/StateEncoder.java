import java.util.ArrayList;
import java.util.Collections;

public class StateEncoder {
    public static long encodeState(ArrayList<Piece> pieces) {
        ArrayList<Integer> a22= new ArrayList<>();
        ArrayList<Integer> a21= new ArrayList<>();
        ArrayList<Integer> a12= new ArrayList<>();
        ArrayList<Integer> a11= new ArrayList<>();

        for (Piece piece : pieces) {
            int topLeft = piece.getTopLeft();

            switch (piece.getType()) {
                case Piece.TWO_BY_TWO:
                    a22.add(topLeft);
                    break;
                case Piece.TWO_BY_ONE:
                    a21.add(topLeft);
                    break;
                case Piece.ONE_BY_TWO:
                    a12.add(topLeft);
                    break;
                case Piece.ONE_BY_ONE:
                    a11.add(topLeft);
                    break;
            }
        }

        Collections.sort(a22); // Not needed, only one 2x2 block
        Collections.sort(a21);
        Collections.sort(a12);
        Collections.sort(a11);

        long key = 0;
        key = packList(key, a22);
        key = packList(key, a21);
        key = packList(key, a12);
        key = packList(key, a11);

        return key;
    }

    private static long packList(long key, ArrayList<Integer> topLefts) {
        for (int cellNum : topLefts) {
            if (cellNum < 0 || cellNum > 19) throw new IllegalArgumentException("Bad cellNum: " + cellNum);
            key = (key << 5) | (long) cellNum;
        }
        return key;
    }
}
