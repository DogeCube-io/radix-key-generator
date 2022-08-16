package io.dogecube.generator;

import com.radixdlt.utils.Pair;

/**
 * Efficient probabilistic cache of fixed capacity to store computed EC points.
 * The more frequently the item is used - the higher probability that it exists in the cache.
 */
public class SimpleIdentityHashMap<K, V> {
    private final Pair<K, V>[] table;
    private final int capacity;

    public SimpleIdentityHashMap(int capacity) {
        table = new Pair[capacity];
        this.capacity = capacity;
    }

    public void put(K key, V value) {
        int cell = hash(key);
        table[cell] = Pair.of(key, value);
    }

    public V get(K key) {
        int cell = hash(key);
        Pair<K, V> val = table[cell];
        boolean hit = val != null && val.getFirst() == key;
        return hit ? val.getSecond() : null;
    }

    private int hash(Object x) {
        int h = System.identityHashCode(x);
        // Multiply by -254 to use the hash LSB and to ensure index is even
        return ((h << 1) - (h << 8)) & (capacity - 1);
    }

}
