package skiplist;

import adt.Entry;
import adt.MapADT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Skip List: a randomized data structure that implements an ordered Map.
 *
 * A skip list is a layered linked list where each level skips over
 * a fraction of the elements, providing expected O(log n) search, insert,
 * and delete, while being simpler to implement than balanced BSTs.
 *
 * Randomized Algorithm: when inserting, a coin flip determines how many
 * levels the new node participates in. With probability 1/2 at each level,
 * the expected height of a node is O(log n).
 *
 * Keys must be Comparable for ordered operations.
 *
 * Structure:
 *   Level 3:  head --------------------------------> tail
 *   Level 2:  head ----------> 30 ----------------> tail
 *   Level 1:  head ----> 15 -> 30 ------> 60 -----> tail
 *   Level 0:  head -> 10 -> 15 -> 30 -> 45 -> 60 -> tail
 */
public class SkipListMap<K extends Comparable<K>, V> implements MapADT<K, V> {

    // ---- Skip List Node ----
    static class SkipNode<K, V> {
        Entry<K, V> entry;
        SkipNode<K, V>[] next; // forward pointers at each level

        @SuppressWarnings("unchecked")
        SkipNode(K key, V value, int levels) {
            this.entry = (key == null) ? null : new Entry<>(key, value);
            this.next = new SkipNode[levels];
        }
    }

    private static final int MAX_LEVEL = 20;
    private SkipNode<K, V> head;
    private int currentLevel; // current highest level in use
    private int size;
    private Random random;

    // Performance tracking
    private long probeCount;
    private long operationCount;

    public SkipListMap() {
        this.head = new SkipNode<>(null, null, MAX_LEVEL);
        this.currentLevel = 0;
        this.size = 0;
        this.random = new Random(42); // fixed seed for reproducibility
        this.probeCount = 0;
        this.operationCount = 0;
    }

    /**
     * Randomized level generation.
     * Flip a coin: with probability 1/2, go up one more level.
     * Expected value = 1 (geometric distribution).
     */
    private int randomLevel() {
        int level = 0;
        while (random.nextDouble() < 0.5 && level < MAX_LEVEL - 1) {
            level++;
        }
        return level;
    }

    // ---- Search in Skip List ----
    // Start from the highest level of the head.
    // At each level, move forward while the next key is less than the target.
    // Drop down a level when blocked. Repeat until level 0.

    @Override
    public V get(K key) {
        operationCount++;
        SkipNode<K, V> current = head;

        // Search from top level down to level 0
        for (int i = currentLevel; i >= 0; i--) {
            while (current.next[i] != null &&
                   current.next[i].entry != null &&
                   current.next[i].entry.getKey().compareTo(key) < 0) {
                current = current.next[i];
                probeCount++;
            }
            probeCount++;
        }

        // Move to the candidate node at level 0
        current = current.next[0];

        if (current != null && current.entry != null &&
            current.entry.getKey().compareTo(key) == 0) {
            return current.entry.getValue();
        }
        return null;
    }

    // ---- Insertion in Skip List ----
    // 1. Search to find position (tracking update[] pointers at each level).
    // 2. Generate random level for new node.
    // 3. Insert by splicing pointers at each level up to the new node's level.

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        operationCount++;
        SkipNode<K, V>[] update = new SkipNode[MAX_LEVEL];
        SkipNode<K, V> current = head;

        // Find position and record update pointers
        for (int i = currentLevel; i >= 0; i--) {
            while (current.next[i] != null &&
                   current.next[i].entry != null &&
                   current.next[i].entry.getKey().compareTo(key) < 0) {
                current = current.next[i];
                probeCount++;
            }
            update[i] = current;
            probeCount++;
        }

        current = current.next[0];

        // If key already exists, update value
        if (current != null && current.entry != null &&
            current.entry.getKey().compareTo(key) == 0) {
            V old = current.entry.getValue();
            current.entry.setValue(value);
            return old;
        }

        // Generate random level for new node
        int newLevel = randomLevel();

        // If new level is higher than current, initialize update pointers
        if (newLevel > currentLevel) {
            for (int i = currentLevel + 1; i <= newLevel; i++) {
                update[i] = head;
            }
            currentLevel = newLevel;
        }

        // Create new node and splice into each level
        SkipNode<K, V> newNode = new SkipNode<>(key, value, newLevel + 1);
        for (int i = 0; i <= newLevel; i++) {
            newNode.next[i] = update[i].next[i];
            update[i].next[i] = newNode;
        }

        size++;
        return null;
    }

    // ---- Deletion in Skip List ----
    // 1. Search to find the node (tracking update[] at each level).
    // 2. If found, remove by updating pointers at each level.
    // 3. Reduce currentLevel if top levels become empty.

    @Override
    @SuppressWarnings("unchecked")
    public V remove(K key) {
        operationCount++;
        SkipNode<K, V>[] update = new SkipNode[MAX_LEVEL];
        SkipNode<K, V> current = head;

        for (int i = currentLevel; i >= 0; i--) {
            while (current.next[i] != null &&
                   current.next[i].entry != null &&
                   current.next[i].entry.getKey().compareTo(key) < 0) {
                current = current.next[i];
                probeCount++;
            }
            update[i] = current;
            probeCount++;
        }

        current = current.next[0];

        if (current == null || current.entry == null ||
            current.entry.getKey().compareTo(key) != 0) {
            return null; // key not found
        }

        V old = current.entry.getValue();

        // Unlink at each level
        for (int i = 0; i <= currentLevel; i++) {
            if (update[i].next[i] != current) break;
            update[i].next[i] = current.next[i];
        }

        // Reduce level if top levels are now empty
        while (currentLevel > 0 && head.next[currentLevel] == null) {
            currentLevel--;
        }

        size--;
        return old;
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
        SkipNode<K, V> current = head.next[0];
        while (current != null && current.entry != null) {
            entries.add(current.entry);
            current = current.next[0];
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

    // ---- Performance Metrics ----

    public double getAverageProbes() {
        return operationCount == 0 ? 0 : (double) probeCount / operationCount;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void resetCounters() {
        probeCount = 0;
        operationCount = 0;
    }

    @Override
    public String toString() {
        return "SkipListMap{size=" + size + ", levels=" + (currentLevel + 1) +
                ", avgProbes=" + String.format("%.2f", getAverageProbes()) + "}";
    }
}
