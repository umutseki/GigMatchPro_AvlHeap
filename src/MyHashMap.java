import java.util.ArrayList;

//i wrote my own hashmap class, this is a basic hashmap class i looked the ones in the book to create it
public class MyHashMap<K, V> {

    private static final int INITIAL_CAPACITY = 1024;
    private static final double LOAD_FACTOR = 0.75;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V>[] buckets;
    private int capacity;
    private int size;

    public MyHashMap() {
        this.capacity = INITIAL_CAPACITY;
        this.buckets = new Node[capacity];
        this.size = 0;
    }

    //creates a hash code for all ids special
    private int hash(K key) {
        return (key.hashCode() & 0x7fffffff) % capacity;
    }

    //O(1) searching
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    //O(1) ulaşma
    public V get(K key) {
        int index = hash(key);
        Node<K, V> curr = buckets[index];

        while (curr != null) {
            if (curr.key.equals(key)) {
                return curr.value;
            }
            curr = curr.next;
        }

        return null;
    }

    //O(1) inserting
    public void put(K key, V value) {
        int index = hash(key);
        Node<K, V> curr = buckets[index];

        while (curr != null) {
            if (curr.key.equals(key)) {
                curr.value = value;
                return;
            }
            curr = curr.next;
        }

        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        size++;

        if ((double) size / capacity >= LOAD_FACTOR) {
            rehash();
        }
    }

    public void remove(K key) {
        int index = hash(key);
        Node<K, V> curr = buckets[index];
        Node<K, V> prev = null;

        while (curr != null) {
            if (curr.key.equals(key)) {
                if (prev == null) {
                    buckets[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return;
            }
            prev = curr;
            curr = curr.next;
        }
    }

    private void rehash() {
        int oldCapacity = capacity;
        Node<K, V>[] oldBuckets = buckets;

        capacity = capacity * 2;
        buckets = new Node[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Node<K, V> node = oldBuckets[i];
            while (node != null) {
                put(node.key, node.value);
                node = node.next;
            }
        }
    }

    public ArrayList<K> keySet() {
        ArrayList<K> keys = new ArrayList<>();

        // table: ArrayList<Node<K,V>>
        for (Node<K, V> node : buckets) {
            Node<K, V> current = node;

            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }

        return keys;
    }

}