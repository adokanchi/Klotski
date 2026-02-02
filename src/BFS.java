public class BFS {
    private LongVisitedSet visited;

    public BFS(int expectedStates) {
        visited = new LongVisitedSet(expectedStates);
    }

    public boolean markExplored(Board board) {
        long key = StateEncoder.encodeState(board.getPieces());
        return visited.addIfAbsent(key); // true = newly discovered
    }

    public static void main(String[] args) {

    }

    private static final class LongVisitedSet {
        private long[] table;   // stores key+1; 0 = empty
        private int size;
        private int resizeAt;

        public LongVisitedSet(int expectedSize) {
            int cap = 1;
            while (cap < (expectedSize * 10) / 7) cap <<= 1; // ~0.7 load factor
            table = new long[cap];
            resizeAt = (cap * 7) / 10;
        }

        /** returns true if newly added, false if already present */
        public boolean addIfAbsent(long key) {
            long stored = key + 1;
            if (stored == 0) throw new IllegalArgumentException("Key overflow"); // basically impossible

            if (size >= resizeAt) rehash();

            int mask = table.length - 1;
            int idx = (int) mix64(key) & mask;

            while (true) {
                long cur = table[idx];
                if (cur == 0) {
                    table[idx] = stored;
                    size++;
                    return true;
                }
                if (cur == stored) return false;
                idx = (idx + 1) & mask;
            }
        }

        private void rehash() {
            long[] old = table;
            table = new long[old.length << 1];
            size = 0;
            resizeAt = (table.length * 7) / 10;

            int mask = table.length - 1;
            for (long stored : old) {
                if (stored == 0) continue;
                long key = stored - 1;
                int idx = (int) mix64(key) & mask;
                while (table[idx] != 0) idx = (idx + 1) & mask;
                table[idx] = stored;
                size++;
            }
        }

        private static long mix64(long z) {
            z ^= (z >>> 33);
            z *= 0xff51afd7ed558ccdL;
            z ^= (z >>> 33);
            z *= 0xc4ceb9fe1a85ec53L;
            z ^= (z >>> 33);
            return z;
        }
    }

}
