package adt;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiset (Bag) ADT: a collection that allows duplicate elements.
 * Each element has an associated count.
 *
 * Implemented using the Adaptor Pattern: wraps a MapADT<E, Integer>
 * where the value is the count of each element.
 */
public class Multiset<E> {

    private MapADT<E, Integer> internalMap;

    /**
     * Adaptor Pattern: any MapADT implementation can back this Multiset.
     */
    public Multiset(MapADT<E, Integer> backingMap) {
        this.internalMap = backingMap;
    }

    /** Add one occurrence of the element. */
    public void add(E element) {
        Integer count = internalMap.get(element);
        internalMap.put(element, count == null ? 1 : count + 1);
    }

    /** Add multiple occurrences of the element. */
    public void add(E element, int count) {
        Integer existing = internalMap.get(element);
        internalMap.put(element, (existing == null ? 0 : existing) + count);
    }

    /** Get the count of the given element. */
    public int count(E element) {
        Integer c = internalMap.get(element);
        return c == null ? 0 : c;
    }

    /** Remove one occurrence. Returns true if the element was present. */
    public boolean remove(E element) {
        Integer c = internalMap.get(element);
        if (c == null || c == 0) return false;
        if (c == 1) {
            internalMap.remove(element);
        } else {
            internalMap.put(element, c - 1);
        }
        return true;
    }

    /** Total number of elements including duplicates. */
    public int size() {
        int total = 0;
        for (Integer c : internalMap.values()) {
            total += c;
        }
        return total;
    }

    /** Number of distinct elements. */
    public int distinctCount() {
        return internalMap.size();
    }

    public boolean contains(E element) {
        return count(element) > 0;
    }

    public List<E> distinctElements() {
        return internalMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Multiset{");
        List<Entry<E, Integer>> entries = internalMap.entrySet();
        for (int i = 0; i < entries.size(); i++) {
            Entry<E, Integer> e = entries.get(i);
            sb.append(e.getKey()).append(":").append(e.getValue());
            if (i < entries.size() - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
