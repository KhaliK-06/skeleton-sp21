package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {

    /**
     * The element in the LLDeque.
     */
    private class TNode {
        public T first;
        public TNode next;
        public TNode prev;

        public TNode(T item, TNode Next, TNode Prev) {
            first = item;
            next = Next;
            prev = Prev;
        }
    }

    private TNode sentinal;
    private int size = 0;

    public LinkedListDeque() {
        sentinal = new TNode(null, null, null);
        sentinal.next = sentinal;
        sentinal.prev = sentinal;
    }

    public LinkedListDeque(T item) {
        sentinal = new TNode(null, null, null);
        sentinal.next = new TNode(item, sentinal, sentinal);
        sentinal.prev = sentinal.next;
        size += 1;
    }

    @Override
    public void addFirst(T item) {
        sentinal.next = new TNode(item, sentinal.next, sentinal);
        sentinal.next.next.prev = sentinal.next;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        sentinal.prev = new TNode(item, sentinal, sentinal.prev);
        sentinal.prev.prev.next = sentinal.prev;
        size += 1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        TNode iter = sentinal.next;
        while (iter.next != sentinal) {
            System.out.print(iter.first + " ");
            iter = iter.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size <= 0) {
            return null;
        }
        T tmp = sentinal.next.first;
        sentinal.next = sentinal.next.next;
        sentinal.next.prev = sentinal;
        size -= 1;
        return tmp;
    }

    @Override
    public T removeLast() {
        if (size <= 0) {
            return null;
        }
        T tmp = sentinal.prev.first;
        sentinal.prev = sentinal.prev.prev;
        sentinal.prev.next = sentinal;
        size -= 1;
        return tmp;
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        TNode iter = sentinal;
        for (int i = 0; i <= index; i += 1) {
            iter = iter.next;
        }
        return iter.first;
    }

    public T getRecursive(int index) {
        if (size < index) {
            return null;
        }
        return helpRecursive(sentinal.next, index);
    }

    /**
     * Help recursive in th gerRecursive.
     */
    private T helpRecursive(TNode t, int index) {
        if (index == 0) {
            return t.first;
        }
        return helpRecursive(t.next, index - 1);
    }

    /**
     * Help to implement iterator()
     */
    private class LinkedListDequeIterator implements Iterator<T> {
        private TNode currentNode = sentinal;

        @Override
        public boolean hasNext() {
            return currentNode.next != sentinal;
        }

        @Override
        public T next() {
            T returnValue = currentNode.first;
            currentNode = currentNode.next;
            return returnValue;
        }

    }

    public Iterator<T> iteator() {
        return new LinkedListDequeIterator();
    }

    /**From Gemini 使用无界通配符<?>来避免泛型擦除.*/
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof LinkedListDeque)) {
            return false;
        }

        LinkedListDeque<?> tmp = (LinkedListDeque<?>) o;

        if (tmp.size != this.size) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            T myItem = this.get(i);
            Object otherItem = tmp.get(i);  //Use Object to avoid type error.

            if (myItem == null) {
                if (otherItem != null) {
                    return false;
                }
            } else {
                if (!myItem.equals(otherItem)) {
                    return false;
                }
            }

        }

        return true;
    }

}
