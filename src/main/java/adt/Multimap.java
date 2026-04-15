package adt;

import java.util.ArrayList;
import java.util.List;

/**
 * Multimap: a Map where each key can be associated with multiple values.
 * Implemented using the Adaptor Pattern on top of our MapADT (HashMap).
 *
 * Internally stores MapADT<K, List<V>> -- adapts a regular Map
 * to provide multimap semantics.
 */
public class Multimap<K, V> {

    private MapADT<K, List<V>> internalMap;

    /**
     * Adaptor Pattern: wraps any MapADT implementation to provide Multimap behavior.
     * The caller can pass a HashTableMap, ListBasedMap, or SkipListMap.
     */
    public Multimap(MapADT<K, List<V>> backingMap) {
        this.internalMap = backingMap;
    }

    /** Add a value for the given key (allows duplicates under same key). */
    public void put(K key, V value) {
        List<V> existing = internalMap.get(key);
        if (existing == null) {
            existing = new ArrayList<>();
            internalMap.put(key, existing);
        }
        existing.add(value);
    }

    /** Get all values associated with the key. Returns empty list if key not found. */
    public List<V> get(K key) {
        List<V> result = internalMap.get(key);
        return result == null ? new ArrayList<>() : result;
    }

    /** Remove one occurrence of value under key. Returns true if removed. */
    public boolean remove(K key, V value) {
        List<V> existing = internalMap.get(key);
        if (existing == null) return false;
        boolean removed = existing.remove(value);
        if (existing.isEmpty()) {
            internalMap.remove(key);
        }
        return removed;
    }

    /** Remove all values for the given key. */
    public List<V> removeAll(K key) {
        List<V> removed = internalMap.remove(key);
        return removed == null ? new ArrayList<>() : removed;
    }

    /** Total number of key-value pairs (counting duplicates). */
    public int size() {
        int total = 0;
        for (List<V> vals : internalMap.values()) {
            total += vals.size();
        }
        return total;
    }

    /** Number of distinct keys. */
    public int keyCount() {
        return internalMap.size();
    }

    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    public boolean containsKey(K key) {
        return internalMap.containsKey(key);
    }

    public List<K> keySet() {
        return internalMap.keySet();
    }
}
