/*
Ryan Demo
rdemo1
600.226.01
P2
 */

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/** Base implementation of MapJHU using linear probe open addressing.
 *  @author CS226 Staff, Spring 2016
 *  @param <K> the base type of the keys
 *  @param <V> the base type of the values
 */
public class LPHashMap<K, V> implements MapJHU<K, V>,
    Iterable<LPMapEntry<K, V>> {

    /** Capacity to initialize bucket array to.
     */
    static final int INITIAL_CAPACITY = 5;

    /** Constant 3.
     */
    static final int THREE = 3;

    /** Maximum load for bucket array.
     */
    public float maxLoad;

    /** Current number of elements in bucket array.
     */
    public int numElements;

    /** Number of tombstones in bucket array.
     */
    public int tombstones;

    /** Capacity of bucket array.
     */
    public int capacity;

    /** Bucket array.
     */
    LPMapEntry<K, V>[] entries;

    /* Custom methods --------------------------------- */

    /** Create an empty open addressing hash map implementation with capacity 5.
     *  @param max the maximum load factor, 0 < maxLoad <= 1
     */
    public LPHashMap(float max) {
        this.maxLoad = max;
        this.numElements = 0;
        this.tombstones = 0;
        this.capacity = LPHashMap.INITIAL_CAPACITY;

        //set up array
        this.entries = new LPMapEntry[LPHashMap.INITIAL_CAPACITY];
    }

    /** Get the maximum load factor.
     *  @return the load factor
     */
    public float getMaxLoad() {
        return this.maxLoad;
    }

    /** Get the current load factor.
     *  @return the load factor, should be 0 < lf <= 1
     */
    public float getLoad() {
        return (float) this.numElements / (float) this.getCapacity();
    }

    /** Get the table capacity (total # of slots).
     *  @return the capacity
     */
    public int getCapacity() {
        return this.capacity;
    }

    /** Rehash the entries to a new table size.
     *  @param cap the capacity of the table after rehashing, cap > size()
     */
    public void rehash(int cap) {
        LPMapEntry<K, V>[] temp = new LPMapEntry[cap];
        
        //iterate through the original array, copying it to increased array
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i] != null && !this.entries[i].isTombstone()) {
                
                //define a probe ticker then iterate to find empty spot
                //only called if item has been hashed to same index of
                //rehashed table
                int probe = 0;
                while (temp[(Math.abs(this.entries[i].getKey().hashCode())
                        + probe) % cap] != null) {
                    probe++;
                }

                temp[(Math.abs(this.entries[i].getKey().hashCode()) + probe)
                     % cap] = this.entries[i];
            }
        }
        this.capacity = cap;
        this.entries = temp;
        this.tombstones = 0;
    }

    /** Get the number of tombstones currently in the map (markers
     *  left behind when values were deleted, until the slot is reused).
     *  @return the number
     */
    public int ghosts() {
        return this.tombstones;
    }

    /* Methods from the MapJHU interface ----------------  */

    /** Get the number of (actual) entries in the Map.
     *  @return the size
     */
    public int size() {
        return this.numElements;
    }

    /** Remove all entries from the Map.
     */
    public void clear() {
        this.numElements = 0;
        this.tombstones = 0;
        this.entries = new LPMapEntry[this.getCapacity()];
    }

    /** Find out if the Map has any entries.
     *  @return true if no entries, false otherwise
     */
    public boolean isEmpty() {
        if (this.numElements == 0) {
            return true;
        }
        return false;
    }

    /** Find out if a key is in the map.
     *  @param key the key being searched for
     *  @return true if found, false otherwise
     */
    public boolean hasKey(K key) {
        for (LPMapEntry<K, V> item: this.entries) {
            if (item != null && item.getKey().equals(key)
                    && !item.isTombstone()) {
                return true;
            }
        }
        return false;
    }

    /** Find out if a value is in the map.
     *  @param value the value to search for
     *  @return true if found, false otherwise
     */
    public boolean hasValue(V value) {
        //iterate over bucket array and return true when found
        for (LPMapEntry<K, V> item: this.entries) {
            if (item != null && !item.isTombstone()
                    && item.getValue().equals(value)) {
                return true;
            }
        }
        
        return false;
    }

    /** Get the value associated with a key if there.
     *  @param key the key being searched for
     *  @return the value associated with key, or null if not found
     */
    public V get(K key) {
        //define a probe
        int probe = 0;
        //iterate over bucket array
        while (this.entries[(Math.abs(key.hashCode()) + probe)
                            % this.getCapacity()]
                != null && probe <= this.capacity) {
            //if keys match and not tombstone, return value
            if (this.entries[(Math.abs(key.hashCode()) + probe)
                % this.getCapacity()].getKey().equals(key)) {
                if (!this.entries[(Math.abs(key.hashCode()) + probe)
                    % this.getCapacity()].isTombstone()) {
                    return this.entries[(Math.abs(key.hashCode()) + probe)
                        % this.getCapacity()].getValue();
                } else {
                    return null;
                }
            }
            probe++;
        }
        return null;
    }

    /** Associate a value with a key, replacing the old value if key exists.
     *  @param key the key for the entry
     *  @param value the value for the entry
     *  @return the old value associated with the key, or null if new entry
     */
    public V put(K key, V value) {
        
        boolean hasRequestedKey = this.hasKey(key);
        
        if (!hasRequestedKey) {
            this.numElements++;
        }

        //check array size
        if (this.getLoad() > this.getMaxLoad()) {
            //rehash with capacity of next prime after 2*capacity
            this.rehash(this.nextPrime(2 * this.getCapacity() + 1));
        }
        
        //check for tombstone imbalance
        if (this.numElements < this.tombstones) {
            this.rehash(this.getCapacity());
        }

        //create object to add to HashMap
        LPMapEntry<K, V> newEntry = new LPMapEntry(key, value);

        //define a probe
        int probe = 0;
        V oldValue = null;
        
        //update value if key is already there (as tombstone or not)
        if (hasRequestedKey) {
            while (this.entries[(Math.abs(key.hashCode()) + probe)
                                % this.getCapacity()] != null) {
                LPMapEntry<K, V> entry = this.entries[(Math.abs(key.hashCode())
                        + probe) % this.getCapacity()];
                if (key.equals(entry.getKey())) {
                    if (!key.equals(entry.isTombstone())) {
                        oldValue = entry.getValue();
                    }
                    this.entries[(Math.abs(key.hashCode()) + probe)
                                 % this.getCapacity()]
                            = newEntry;
                    return oldValue;
                } else {
                    probe++;
                }
            }
        } else { //otherwise create the entry at next available position
            while (this.entries[(Math.abs(key.hashCode()) + probe)
                                % this.getCapacity()] != null) {
                LPMapEntry<K, V> entry = this.entries[(Math.abs(key.hashCode())
                        + probe) % this.getCapacity()];
                if (entry.isTombstone()) {
                    this.entries[(Math.abs(key.hashCode()) + probe)
                                 % this.getCapacity()]
                            = null;
                    this.tombstones--;
                } else {
                    probe++;
                }
            }
            this.entries[(Math.abs(key.hashCode()) + probe)
                         % this.getCapacity()]
                    = newEntry;
        }
        //oldValue is null if hashed index was empty or was tombstone
        return oldValue;
    }
    
    
    /** Returns the next prime number given an integer.
     * @param lowerBound lowest number the prime can be
     * @return the next prime after the lower bound
     */
    private int nextPrime(int lowerBound) {
        int nextPrime = lowerBound;
        
        //make lower bound odd if even
        if (nextPrime % 2 == 0) {
            nextPrime++;
        }
        
        while (!this.isPrime(nextPrime)) {
            nextPrime += 2;
        }
        
        return nextPrime;
    }

    /** Checks if a number is prime.
     * @param n an integer to check if prime
     * @return true if n is prime, false otherwise
     */
    private boolean isPrime(int n) {
        if (n % 2 == 0) {
            //except for 2, n is not prime if it is even
            return false;
        }
        for (int i = LPHashMap.THREE; i * i <= n; i += 2) {
            if (n % i == 0) {
                //check if any number up to sqrt(n)
                return false;
            }
        }
        return true;
    }
    
    /** Remove the entry associated with a key.
     *  @param key the key for the entry being deleted
     *  @return the value associated with the key, or null if key not there
     */
    public V remove(K key) {
        //define a probe
        int probe = 0;
        V value = null;
        
        //make matched key a tombstone
        while (value == null && this.entries[(Math.abs(key.hashCode()) + probe)
                            % this.getCapacity()] != null) {
            LPMapEntry<K, V> entry = this.entries[(Math.abs(key.hashCode())
                    + probe) % this.getCapacity()];
            if (entry.getKey().equals(key)) {
                if (entry.isTombstone()) {
                    //item already removed, so return null
                    return null;
                } else {
                    value = entry.getValue();
                    entry.makeTombstone();
                    this.tombstones++;
                    this.numElements--;
                }
            }
            probe++;
        }
        
        //rehash if there's too many tombstones now
        if (this.numElements < this.tombstones) {
            this.rehash(this.getCapacity());
        }
        
        //return value before making it a tombstone
        return value;
    }

    // You may use HashSet within this method.
    /** Get a set of all the entries in the map.
     *  @return the set
     */
    public Set<Map.Entry<K, V>> entries() {
        //add all non-null and non-tombstone entries to a HashSet
        HashSet<Map.Entry<K, V>> setEntries = new HashSet<Map.Entry<K, V>>();
        for (LPMapEntry<K, V> item: this.entries) {
            if (item != null && !item.isTombstone()) {
                setEntries.add(item);
            }
        }
        return setEntries;
    }

    // You may use HashSet within this method.
    /** Get a set of all the keys in the map.
     *  @return the set
     */
    public Set<K> keys() {
        Set<K> setKeys = new HashSet<K>();
        //add all non-null and non-tombstone keys to a HashSet
        for (LPMapEntry<K, V> item: this.entries) {
            if (item != null && !item.isTombstone()) {
                setKeys.add(item.getKey());
            }
        }
        return setKeys;
    }

    /** Get a collection of all the values in the map.
     *  @return the collection
     */
    public Collection<V> values() {
        //add all non-null and non-tombstone values to an ArrayList
        ArrayList<V> collectionValues = new ArrayList<V>();
        for (LPMapEntry<K, V> item: this.entries) {
            if (item != null && !item.isTombstone()) {
                collectionValues.add(item.getValue());
            }
        }
        return collectionValues;
    }

    /*  --------------  from Object --------  YOU DON'T HAVE TO IMPLEMENT
    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
     */
    
    
    /* ---------- from Iterable ---------- */
    @Override
    public Iterator<LPMapEntry<K, V>> iterator() {
        return new HashMapIterator();
    }

    @Override
    public void forEach(Consumer<? super LPMapEntry<K, V>> action) {
        // you do not have to implement this
    }

    @Override
    public Spliterator<LPMapEntry<K, V>> spliterator() {
        // you do not have to implement this
        return null;
    }
    
    

    /* -----  HashMapIterator inner class ----- */

    /**
     * Inner HashMapIterator class for convenience.
     * Note that the generic type is implied since we are within DLList<T>.
     */
    public class HashMapIterator implements Iterator<LPMapEntry<K, V>> {

        /** Keeps track of the position of the iterator in the bucket array.
         */
        private int pos;
        
        /**
         * Holds number of entries and tombstones in bucket array that
         * the iterator is initialized to. Uses to check for concurrent
         * modification.
         */
        private int numEntriesAndTombstones;

        /**
         * Make a HashMapIterator.
         */
        public HashMapIterator() {
            this.pos = -1; //start at -1 because next ++'s pos
            this.numEntriesAndTombstones = LPHashMap.this.numElements
                    + LPHashMap.this.ghosts();
        }

        @Override
        public boolean hasNext() {
            if (this.pos + 1 < LPHashMap.this.entries.length) {
                return true;
            }
            return false;
        }

        @Override
        public LPMapEntry<K, V> next() {
            //check if map has been changed 
            if (LPHashMap.this.numElements + LPHashMap.this.ghosts()
                != this.numEntriesAndTombstones) {
                throw new ConcurrentModificationException();
            }
            
            this.pos++;
            return LPHashMap.this.entries[this.pos];
        }

        @Override
        public void remove() {
            if (LPHashMap.this.entries[this.pos] != null
                    && !LPHashMap.this.entries[this.pos].isTombstone()) {
                LPHashMap.this.entries[this.pos].makeTombstone();
                LPHashMap.this.numElements--;
                LPHashMap.this.tombstones++;
            }
        }
    }
}