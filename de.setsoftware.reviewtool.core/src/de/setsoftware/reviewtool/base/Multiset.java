package de.setsoftware.reviewtool.base;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple multiset.
 * @param <T> The item type.
 */
public class Multiset<T> {

    /**
     * Mutable counter value.
     */
    private static final class Counter {
        private int cnt;
    }

    private final Map<T, Counter> items = new HashMap<>();

    /**
     * Add an item to the multiset.
     */
    public void add(T item) {
        Counter c = this.items.get(item);
        if (c == null) {
            c = new Counter();
            this.items.put(item, c);
        }
        c.cnt++;
    }

    /**
     * Returns true iff the given is contained at least once in the map.
     */
    public boolean contains(T item) {
        final Counter c = this.items.get(item);
        return c != null;
    }

    /**
     * Removes one occurrence of the given item from this multiset.
     */
    public void remove(T item) {
        final Counter c = this.items.get(item);
        if (c == null) {
            return;
        }
        c.cnt--;
        if (c.cnt <= 0) {
            this.items.remove(item);
        }
    }

}
