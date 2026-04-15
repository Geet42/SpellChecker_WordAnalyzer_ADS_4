package adt;

import java.util.List;

/**
 * Map ADT Interface.
 * A collection of Entry(key, value) pairs where each key is unique.
 * Supports the fundamental operations: get, put, remove, size, isEmpty, entrySet, keySet, values.
 */
public interface MapADT<K, V> {

    /** Returns the number of entries in the map. */
    int size();

    /** Returns true if the map contains no entries. */
    boolean isEmpty();

    /** Returns the value associated with key, or null if not found. */
    V get(K key);

    /** Associates key with value. Returns the old value if key existed, null otherwise. */
    V put(K key, V value);

    /** Removes the entry with the given key. Returns the removed value, or null. */
    V remove(K key);

    /** Returns true if the map contains an entry with the given key. */
    boolean containsKey(K key);

    /** Returns a list of all entries in the map. */
    List<Entry<K, V>> entrySet();

    /** Returns a list of all keys in the map. */
    List<K> keySet();

    /** Returns a list of all values in the map. */
    List<V> values();
}
