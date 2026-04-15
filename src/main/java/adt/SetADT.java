package adt;

import java.util.List;

/**
 * Set ADT: a collection of unique elements with no duplicates.
 *
 * Implemented using the Adaptor Pattern: wraps a MapADT<E, Boolean>,
 * using keys as the set elements and ignoring the values.
 *
 * Supports standard set operations: union, intersection, difference.
 */
public class SetADT<E> {

    private static final Boolean PRESENT = Boolean.TRUE;
    private MapADT<E, Boolean> internalMap;

    /**
     * Adaptor Pattern: any MapADT implementation can back this Set.
     */
    public SetADT(MapADT<E, Boolean> backingMap) {
        this.internalMap = backingMap;
    }

    public void add(E element) {
        internalMap.put(element, PRESENT);
    }

    public boolean contains(E element) {
        return internalMap.containsKey(element);
    }

    public boolean remove(E element) {
        return internalMap.remove(element) != null;
    }

    public int size() {
        return internalMap.size();
    }

    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    public List<E> elements() {
        return internalMap.keySet();
    }

    /** Union: returns a new set containing all elements from both sets. */
    public SetADT<E> union(SetADT<E> other, MapADT<E, Boolean> backingMap) {
        SetADT<E> result = new SetADT<>(backingMap);
        for (E e : this.elements()) result.add(e);
        for (E e : other.elements()) result.add(e);
        return result;
    }

    /** Intersection: returns a new set containing only elements in both sets. */
    public SetADT<E> intersection(SetADT<E> other, MapADT<E, Boolean> backingMap) {
        SetADT<E> result = new SetADT<>(backingMap);
        for (E e : this.elements()) {
            if (other.contains(e)) result.add(e);
        }
        return result;
    }

    /** Difference: returns elements in this set but not in other. */
    public SetADT<E> difference(SetADT<E> other, MapADT<E, Boolean> backingMap) {
        SetADT<E> result = new SetADT<>(backingMap);
        for (E e : this.elements()) {
            if (!other.contains(e)) result.add(e);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Set" + elements().toString();
    }
}
