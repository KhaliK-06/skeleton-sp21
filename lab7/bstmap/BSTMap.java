package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;

public class BSTMap <K extends Comparable<K>, V> implements Map61B<K, V>{

    private int size = 0;

    private BSTNode root;

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(BSTNode node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.print(node.key + " ");
        printInOrder(node.right);
    }

    //return the node with key in k
    private BSTNode getNode(BSTNode node, K k) {
        if (node == null) {
            return null;
        }
        int cmp = k.compareTo(node.key);
        if (cmp > 0) {
            return getNode(node.right, k);
        } else if (cmp < 0) {
            return getNode(node.left, k);
        } else {
            return node;
        }
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode temp = getNode(root, key);
        return temp != null;
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return get(node.right, key);
        } else if (cmp < 0) {
            return get(node.left, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value);
            size += 1;
        } else {
            root = put(root, key, value);
        }
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            size += 1;
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public V remove(K key) {


        V reValue = get(key);
        root = remove(root, key);
        size -= 1;
        return reValue;
    }

    private BSTNode remove(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            BSTNode temp = min(node.right);
            node.key = temp.key;
            node.value = temp.value;
            node.right = remove(node.right, temp.key);
        }
        return node;
    }

    @Override
    public V remove(K key, V value) {
        if (!get(key).equals(value)) {
            return null;
        }
        remove(key);
        return value;
    }

    private BSTNode min(BSTNode node) {
        if (node.left == null) {
            return node;
        } else {
            return min(node.left);
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    private class BSTMapIter implements Iterator<K> {
        LinkedList<BSTNode> list;

        public BSTMapIter() {
            list = new LinkedList<>();
            list.addLast(root);
        }

        @Override
        public boolean hasNext() {
            return !list.isEmpty();
        }

        @Override
        public K next() {
            BSTNode node = list.removeFirst();
            if (node.left != null) {
                list.addLast(node.left);
            }
            if (node.right != null) {
                list.addLast(node.right);
            }
            return node.key;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (K k : this) {
            set.add(k);
        }
        return set;
    }

    private class BSTNode {

        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;

        BSTNode(K k, V v){
            key = k;
            value = v;
            left = null;
            right = null;
        }
    }

}
