package adt;

import java.util.ArrayList;
import java.util.List;

/**
 * List-Based Map: simplest Map ADT implementation.
 * Stores entries in an unsorted ArrayList.
 *
 * Time complexities:
 *   get    -> O(n)  (linear scan)
 *   put    -> O(n)  (scan for duplicate, then insert)
 *   remove -> O(n)  (scan to find)
 *
 * Used as a baseline to compare against Hash Table and Skip List.
 */
public class ListBasedMap<K, V> implements MapADT<K, V> {

    private List<Entry<K, V>> table;

    // Performance tracking
    private long comparisons;

    public ListBasedMap() {
        table = new ArrayList<>();
        comparisons = 0;
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

    /** Linear scan to find the entry with the given key. Returns index or -1. */
    private int findIndex(K key) {
        for (int i = 0; i < table.size(); i++) {
            comparisons++;
            if (table.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public V get(K key) {
        int idx = findIndex(key);
        if (idx == -1) return null;
        return table.get(idx).getValue();
    }

    @Override
    public V put(K key, V value) {
        int idx = findIndex(key);
        if (idx != -1) {
            V old = table.get(idx).getValue();
            table.get(idx).setValue(value);
            return old;
        }
        table.add(new Entry<>(key, value));
        return null;
    }

    @Override
    public V remove(K key) {
        int idx = findIndex(key);
        if (idx == -1) return null;
        V old = table.get(idx).getValue();
        // Swap with last and remove (O(1) removal after O(n) search)
        table.set(idx, table.get(table.size() - 1));
        table.remove(table.size() - 1);
        return old;
    }

    @Override
    public boolean containsKey(K key) {
        return findIndex(key) != -1;
    }

    @Override
    public List<Entry<K, V>> entrySet() {
        return new ArrayList<>(table);
    }

    @Override
    public List<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> e : table) keys.add(e.getKey());
        return keys;
    }

    @Override
    public List<V> values() {
        List<V> vals = new ArrayList<>();
        for (Entry<K, V> e : table) vals.add(e.getValue());
        return vals;
    }

    public long getComparisons() {
        return comparisons;
    }

    public void resetComparisons() {
        comparisons = 0;
    }
}
