package hashtable;

import adt.Entry;
import adt.MapADT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Hash Table with Separate Chaining: implements MapADT.
 *
 * Each bucket is a linked list (chain) of Entry objects.
 * On collision, the new entry is appended to the chain at that index.
 *
 * Hash Code: computed from the key using Java's hashCode() method.
 *            For strings, this is effectively polynomial hashing:
 *            h = s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
 *
 * Compression Function: configurable between Division and MAD methods.
 *
 * Load Factor: alpha = n / N (entries / capacity).
 * Rehashing: when load factor exceeds threshold, we double capacity and reinsert all entries.
 */
public class HashTableSC<K, V> implements MapADT<K, V> {

    /**
     * Compression function strategy.
     * - DIVISION: h(k) = |hashCode(k)| mod N
     * - MAD (Multiply-Add-Divide): h(k) = |a * hashCode(k) + b| mod p mod N
     *   where p is a prime > N, and a,b are random with a != 0
     */
    public enum CompressionMethod {
        DIVISION,
        MAD
    }

    private LinkedList<Entry<K, V>>[] buckets;
    private int size;
    private int capacity;
    private double loadFactorThreshold;
    private CompressionMethod compressionMethod;

    // MAD parameters: ((a * hashcode + b) mod p) mod N
    private int madA;   // multiplier, must be > 0
    private int madB;   // shift
    private int madP;   // prime larger than capacity

    // Performance tracking
    private long probeCount;
    private long operationCount;
    private int rehashCount;

    private static final int DEFAULT_CAPACITY = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    public HashTableSC() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, CompressionMethod.DIVISION);
    }

    public HashTableSC(int capacity, double loadFactorThreshold, CompressionMethod method) {
        this.capacity = capacity;
        this.loadFactorThreshold = loadFactorThreshold;
        this.compressionMethod = method;
        this.buckets = new LinkedList[capacity];
        this.size = 0;
        this.probeCount = 0;
        this.operationCount = 0;
        this.rehashCount = 0;

        // MAD parameters
        this.madP = nextPrime(capacity * 2);
        this.madA = 7;  // chosen constant > 0
        this.madB = 3;  // chosen constant >= 0
    }

    // ---- Compression Functions ----

    private int compress(K key) {
        int hashCode = key.hashCode();
        hashCode = hashCode & 0x7FFFFFFF; // ensure non-negative

        switch (compressionMethod) {
            case MAD:
                // MAD Method: ((a * hashCode + b) mod p) mod N
                return (int) (((long) madA * hashCode + madB) % madP) % capacity;
            case DIVISION:
            default:
                // Division Method: hashCode mod N
                return hashCode % capacity;
        }
    }

    // ---- MapADT Operations ----

    @Override
    public V get(K key) {
        operationCount++;
        int idx = compress(key);
        if (buckets[idx] == null) {
            probeCount++;
            return null;
        }
        for (Entry<K, V> entry : buckets[idx]) {
            probeCount++;
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        operationCount++;

        // Rehash if load factor exceeded
        if ((double) size / capacity >= loadFactorThreshold) {
            rehash();
        }

        int idx = compress(key);
        if (buckets[idx] == null) {
            buckets[idx] = new LinkedList<>();
        }

        // Check if key already exists in chain
        for (Entry<K, V> entry : buckets[idx]) {
            probeCount++;
            if (entry.getKey().equals(key)) {
                V old = entry.getValue();
                entry.setValue(value);
                return old;
            }
        }

        // Key not found, add new entry
        probeCount++;
        buckets[idx].add(new Entry<>(key, value));
        size++;
        return null;
    }

    @Override
    public V remove(K key) {
        operationCount++;
        int idx = compress(key);
        if (buckets[idx] == null) {
            probeCount++;
            return null;
        }

        var iterator = buckets[idx].iterator();
        while (iterator.hasNext()) {
            probeCount++;
            Entry<K, V> entry = iterator.next();
            if (entry.getKey().equals(key)) {
                V old = entry.getValue();
                iterator.remove();
                size--;
                return old;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public List<Entry<K, V>> entrySet() {
        List<Entry<K, V>> entries = new ArrayList<>();
        for (LinkedList<Entry<K, V>> chain : buckets) {
            if (chain != null) entries.addAll(chain);
        }
        return entries;
    }

    @Override
    public List<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> e : entrySet()) keys.add(e.getKey());
        return keys;
    }

    @Override
    public List<V> values() {
        List<V> vals = new ArrayList<>();
        for (Entry<K, V> e : entrySet()) vals.add(e.getValue());
        return vals;
    }

    // ---- Rehashing ----

    /**
     * Rehashing: doubles capacity and reinserts all entries.
     * This is triggered when load factor alpha = n/N exceeds the threshold.
     * All entries are re-compressed to their new bucket positions.
     */
    private void rehash() {
        rehashCount++;
        LinkedList<Entry<K, V>>[] oldBuckets = buckets;
        capacity = capacity * 2;
        buckets = new LinkedList[capacity];
        size = 0;

        // Update MAD prime
        madP = nextPrime(capacity * 2);

        for (LinkedList<Entry<K, V>> chain : oldBuckets) {
            if (chain != null) {
                for (Entry<K, V> entry : chain) {
                    put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // ---- Performance Metrics ----

    public double getLoadFactor() {
        return (double) size / capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getAverageProbes() {
        return operationCount == 0 ? 0 : (double) probeCount / operationCount;
    }

    public int getRehashCount() {
        return rehashCount;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public int getMaxChainLength() {
        int max = 0;
        for (LinkedList<Entry<K, V>> chain : buckets) {
            if (chain != null && chain.size() > max) {
                max = chain.size();
            }
        }
        return max;
    }

    /** Chain length distribution for analysis. Index i = number of buckets with chain length i. */
    public int[] getChainDistribution() {
        int maxLen = getMaxChainLength();
        int[] dist = new int[maxLen + 1];
        for (LinkedList<Entry<K, V>> chain : buckets) {
            dist[chain == null ? 0 : chain.size()]++;
        }
        return dist;
    }

    public void resetCounters() {
        probeCount = 0;
        operationCount = 0;
    }

    // ---- Utility ----

    private static int nextPrime(int n) {
        if (n <= 2) return 2;
        int candidate = n;
        if (candidate % 2 == 0) candidate++;
        while (!isPrime(candidate)) candidate += 2;
        return candidate;
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HashTableSC{size=" + size + ", capacity=" + capacity +
                ", loadFactor=" + String.format("%.2f", getLoadFactor()) +
                ", compression=" + compressionMethod +
                ", avgProbes=" + String.format("%.2f", getAverageProbes()) +
                ", maxChain=" + getMaxChainLength() +
                ", rehashes=" + rehashCount + "}";
    }
}
