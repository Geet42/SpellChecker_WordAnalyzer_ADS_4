package experiments;

import adt.ListBasedMap;
import hashtable.HashTableSC;
import hashtable.HashTableOA;
import skiplist.SkipListMap;

import java.util.Random;

/**
 * Performance Experiments comparing all Map implementations.
 *
 * Experiment 1: Collision Resolution -- Separate Chaining vs Linear Probing vs Double Hashing
 * Experiment 2: Compression Functions -- Division Method vs MAD Method
 * Experiment 3: Load Factor and Rehashing analysis
 * Experiment 4: Scalability -- List-Based Map vs Hash Tables vs Skip List
 * Experiment 5: Clustering analysis in Open Addressing
 */
public class ExperimentRunner {

    private static final Random random = new Random(42);

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("   PERFORMANCE EXPERIMENTS");
        System.out.println("============================================================\n");

        experiment1_CollisionResolution();
        experiment2_CompressionFunctions();
        experiment3_LoadFactorRehashing();
        experiment4_Scalability();
        experiment5_ClusteringAnalysis();
    }

    // ================================================================
    // Experiment 1: Collision Resolution Strategies
    // Compare: Separate Chaining, Linear Probing, Double Hashing
    // ================================================================
    static void experiment1_CollisionResolution() {
        System.out.println("--- Experiment 1: Collision Resolution Strategies ---");
        System.out.println("Insert 10,000 random strings, then look up all 10,000.\n");

        int N = 10000;
        String[] keys = generateRandomStrings(N, 8);

        System.out.println(String.format("%-25s %10s %10s %10s %10s",
                "Strategy", "AvgProbes", "MaxMetric", "Rehashes", "Time(ms)"));
        System.out.println("-".repeat(68));

        // Separate Chaining
        HashTableSC<String, Integer> sc = new HashTableSC<>(16, 0.75,
                HashTableSC.CompressionMethod.DIVISION);
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) sc.put(keys[i], i);
        for (int i = 0; i < N; i++) sc.get(keys[i]);
        long scTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println(String.format("%-25s %10.2f %10d %10d %10d",
                "Separate Chaining", sc.getAverageProbes(), sc.getMaxChainLength(),
                sc.getRehashCount(), scTime));

        // Linear Probing
        HashTableOA<String, Integer> lp = new HashTableOA<>(16, 0.5,
                HashTableOA.ProbingMethod.LINEAR_PROBING);
        start = System.nanoTime();
        for (int i = 0; i < N; i++) lp.put(keys[i], i);
        for (int i = 0; i < N; i++) lp.get(keys[i]);
        long lpTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println(String.format("%-25s %10.2f %10d %10d %10d",
                "Linear Probing", lp.getAverageProbes(), lp.getLongestCluster(),
                lp.getRehashCount(), lpTime));

        // Double Hashing
        HashTableOA<String, Integer> dh = new HashTableOA<>(16, 0.5,
                HashTableOA.ProbingMethod.DOUBLE_HASHING);
        start = System.nanoTime();
        for (int i = 0; i < N; i++) dh.put(keys[i], i);
        for (int i = 0; i < N; i++) dh.get(keys[i]);
        long dhTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println(String.format("%-25s %10.2f %10d %10d %10d",
                "Double Hashing", dh.getAverageProbes(), dh.getLongestCluster(),
                dh.getRehashCount(), dhTime));

        System.out.println("(MaxMetric = max chain length for SC, longest cluster for OA)\n");
    }

    // ================================================================
    // Experiment 2: Compression Functions -- Division vs MAD
    // ================================================================
    static void experiment2_CompressionFunctions() {
        System.out.println("--- Experiment 2: Compression Functions (Division vs MAD) ---");
        System.out.println("Insert 10,000 strings into HashTableSC with each method.\n");

        int N = 10000;
        String[] keys = generateRandomStrings(N, 10);

        System.out.println(String.format("%-15s %10s %10s %10s",
                "Compression", "AvgProbes", "MaxChain", "Time(ms)"));
        System.out.println("-".repeat(48));

        for (HashTableSC.CompressionMethod method : HashTableSC.CompressionMethod.values()) {
            HashTableSC<String, Integer> table = new HashTableSC<>(16, 0.75, method);
            long start = System.nanoTime();
            for (int i = 0; i < N; i++) table.put(keys[i], i);
            for (int i = 0; i < N; i++) table.get(keys[i]);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            System.out.println(String.format("%-15s %10.2f %10d %10d",
                    method.name(), table.getAverageProbes(),
                    table.getMaxChainLength(), elapsed));

            // Show chain distribution
            int[] dist = table.getChainDistribution();
            System.out.print("  Chains: ");
            for (int i = 0; i < Math.min(dist.length, 7); i++) {
                System.out.print("len" + i + "=" + dist[i] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // ================================================================
    // Experiment 3: Load Factor and Rehashing
    // ================================================================
    static void experiment3_LoadFactorRehashing() {
        System.out.println("--- Experiment 3: Load Factor Thresholds and Rehashing ---");
        System.out.println("Insert 10,000 strings with different load factor thresholds.\n");

        int N = 10000;
        String[] keys = generateRandomStrings(N, 8);

        // Test on Separate Chaining
        System.out.println("Separate Chaining:");
        System.out.println(String.format("%-10s %10s %10s %10s %10s %10s",
                "LF Thresh", "FinalCap", "ActualLF", "AvgProbes", "Rehashes", "Time(ms)"));
        System.out.println("-".repeat(64));

        double[] thresholds = {0.5, 0.75, 1.0, 1.5, 2.0};
        for (double lf : thresholds) {
            HashTableSC<String, Integer> table = new HashTableSC<>(16, lf,
                    HashTableSC.CompressionMethod.DIVISION);
            long start = System.nanoTime();
            for (int i = 0; i < N; i++) table.put(keys[i], i);
            for (int i = 0; i < N; i++) table.get(keys[i]);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            System.out.println(String.format("%-10.2f %10d %10.2f %10.2f %10d %10d",
                    lf, table.getCapacity(), table.getLoadFactor(),
                    table.getAverageProbes(), table.getRehashCount(), elapsed));
        }

        // Test on Open Addressing (Linear Probing)
        System.out.println("\nLinear Probing:");
        System.out.println(String.format("%-10s %10s %10s %10s %10s %10s",
                "LF Thresh", "FinalCap", "ActualLF", "AvgProbes", "Rehashes", "Time(ms)"));
        System.out.println("-".repeat(64));

        double[] oaThresholds = {0.3, 0.5, 0.6, 0.7};
        for (double lf : oaThresholds) {
            HashTableOA<String, Integer> table = new HashTableOA<>(16, lf,
                    HashTableOA.ProbingMethod.LINEAR_PROBING);
            long start = System.nanoTime();
            for (int i = 0; i < N; i++) table.put(keys[i], i);
            for (int i = 0; i < N; i++) table.get(keys[i]);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            System.out.println(String.format("%-10.2f %10d %10.2f %10.2f %10d %10d",
                    lf, table.getCapacity(), table.getLoadFactor(),
                    table.getAverageProbes(), table.getRehashCount(), elapsed));
        }
        System.out.println();
    }

    // ================================================================
    // Experiment 4: Scalability -- All Implementations
    // List-Based Map O(n) vs Hash Tables O(1) avg vs Skip List O(log n)
    // ================================================================
    static void experiment4_Scalability() {
        System.out.println("--- Experiment 4: Scalability Comparison ---");
        System.out.println("Insert N elements then look up all N.\n");

        int[] sizes = {500, 1000, 2000, 5000, 10000};

        System.out.println(String.format("%-8s %12s %12s %12s %12s",
                "N", "ListMap(ms)", "HT-SC(ms)", "HT-OA(ms)", "SkipList(ms)"));
        System.out.println("-".repeat(60));

        for (int N : sizes) {
            String[] keys = generateRandomStrings(N, 8);

            // ListBasedMap
            ListBasedMap<String, Integer> listMap = new ListBasedMap<>();
            long start = System.nanoTime();
            for (int i = 0; i < N; i++) listMap.put(keys[i], i);
            for (int i = 0; i < N; i++) listMap.get(keys[i]);
            long listTime = (System.nanoTime() - start) / 1_000_000;

            // HashTableSC
            HashTableSC<String, Integer> scMap = new HashTableSC<>(16, 0.75,
                    HashTableSC.CompressionMethod.DIVISION);
            start = System.nanoTime();
            for (int i = 0; i < N; i++) scMap.put(keys[i], i);
            for (int i = 0; i < N; i++) scMap.get(keys[i]);
            long scTime = (System.nanoTime() - start) / 1_000_000;

            // HashTableOA (Double Hashing)
            HashTableOA<String, Integer> oaMap = new HashTableOA<>(16, 0.5,
                    HashTableOA.ProbingMethod.DOUBLE_HASHING);
            start = System.nanoTime();
            for (int i = 0; i < N; i++) oaMap.put(keys[i], i);
            for (int i = 0; i < N; i++) oaMap.get(keys[i]);
            long oaTime = (System.nanoTime() - start) / 1_000_000;

            // SkipListMap
            SkipListMap<String, Integer> skipMap = new SkipListMap<>();
            start = System.nanoTime();
            for (int i = 0; i < N; i++) skipMap.put(keys[i], i);
            for (int i = 0; i < N; i++) skipMap.get(keys[i]);
            long skipTime = (System.nanoTime() - start) / 1_000_000;

            System.out.println(String.format("%-8d %12d %12d %12d %12d",
                    N, listTime, scTime, oaTime, skipTime));
        }

        System.out.println("\nExpected: ListMap grows quadratically, Hash Tables stay near-constant,");
        System.out.println("          SkipList grows as O(n log n).\n");
    }

    // ================================================================
    // Experiment 5: Clustering in Open Addressing
    // Compare Linear Probing (primary clustering) vs Double Hashing
    // ================================================================
    static void experiment5_ClusteringAnalysis() {
        System.out.println("--- Experiment 5: Clustering Analysis (Open Addressing) ---");
        System.out.println("Insert 5,000 strings with initial capacity 16, load factor 0.6.\n");

        int N = 5000;
        String[] keys = generateRandomStrings(N, 8);

        System.out.println(String.format("%-20s %10s %10s %12s %10s",
                "Probing", "AvgProbes", "Clusters", "LongestRun", "FinalCap"));
        System.out.println("-".repeat(65));

        // Linear Probing
        HashTableOA<String, Integer> lp = new HashTableOA<>(16, 0.6,
                HashTableOA.ProbingMethod.LINEAR_PROBING);
        for (int i = 0; i < N; i++) lp.put(keys[i], i);
        lp.resetCounters();
        for (int i = 0; i < N; i++) lp.get(keys[i]);

        System.out.println(String.format("%-20s %10.2f %10d %12d %10d",
                "Linear Probing", lp.getAverageProbes(), lp.getClusterCount(),
                lp.getLongestCluster(), lp.getCapacity()));

        // Double Hashing
        HashTableOA<String, Integer> dh = new HashTableOA<>(16, 0.6,
                HashTableOA.ProbingMethod.DOUBLE_HASHING);
        for (int i = 0; i < N; i++) dh.put(keys[i], i);
        dh.resetCounters();
        for (int i = 0; i < N; i++) dh.get(keys[i]);

        System.out.println(String.format("%-20s %10.2f %10d %12d %10d",
                "Double Hashing", dh.getAverageProbes(), dh.getClusterCount(),
                dh.getLongestCluster(), dh.getCapacity()));

        System.out.println("\nExpected: Linear Probing shows fewer but larger clusters (primary clustering).");
        System.out.println("Double Hashing distributes entries more evenly with shorter clusters.\n");
    }

    // ---- Utility ----
    private static String[] generateRandomStrings(int count, int length) {
        String[] result = new String[count];
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder(length);
            for (int j = 0; j < length; j++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            result[i] = sb.toString();
        }
        return result;
    }
}
