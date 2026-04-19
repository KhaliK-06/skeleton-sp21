package hashmap;

import edu.neu.ccs.HexXShort;

import java.rmi.UnexpectedException;
import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private double loadFactor = 0.75;
    private HashSet<K> keySet = new HashSet<K>();
    private int size = 0;

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(16);
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] tmp = new Collection[tableSize];
        for (int i = 0; i < tmp.length; i += 1) {
            tmp[i] = createBucket();
        }
        return tmp;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    private void resize() {
        Collection<Node>[] tmp = createTable(2 * buckets.length);
        for (K key : keySet) {
            tmp[mod(key.hashCode(), tmp.length)].add(createNode(key, get(key)));
        }
        buckets = tmp;
    }

    private int mod(int value, int size) {
        return (value % size + size) % size;
    }

    @Override
    public void clear() {
        buckets = createTable(16);
        keySet = new HashSet<K>();
        loadFactor = 0.75;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return keySet.contains(key);
    }

    @Override
    public V get(K key) {
        int index = mod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            return null;
        }
        for (Node currentNode : buckets[index]) {
            if (currentNode.key.equals(key)) {
                return currentNode.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if ((size + 1) * 1.0 / buckets.length > loadFactor) {
            resize();
        }
        if (keySet.contains(key)) {
            for (Node currentNode : buckets[mod(key.hashCode(), buckets.length)]) {
                if (currentNode.key.equals(key)) {
                    currentNode.value = value;
                }
            }
        } else {
            keySet.add(key);
            buckets[mod(key.hashCode(), buckets.length)].add(createNode(key, value));
            size += 1;
        }
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = mod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[index];
        for (Node currentNode : bucket) {
            if (currentNode.key.equals(key)) {
                V returnValue = currentNode.value;
                bucket.remove(currentNode);
                keySet.remove(key);
                size -= 1;
                return returnValue;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        if (get(key).equals(value)) {
            return remove(key);
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet.iterator();
    }

}
