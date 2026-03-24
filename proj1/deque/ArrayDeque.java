package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    /** items : the container of ADeque.
     * size : the size of Deque.(not the length of items)
     * first : Point to the next first element.
     * last : Point to the next last element.*/
    private T[] items;
    private int size = 0;
    private int first;
    private int last;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        first = 3;
        last = 4;
    }

    private void reSize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        if (last > first) {
            System.arraycopy(items, first, a, 0, size);
        }
        System.arraycopy(items, first, a, 0, size - first);
        System.arraycopy(items, 0, a, size - first, first);
        items = a;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            reSize(2 * size);
        }
        items[first] = item;
        first = (first - 1 + items.length) % items.length;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            reSize(2 * size);
        }
        items[last] = item;
        last = (last + 1) % items.length;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Iterator<T> iter = iterator();
        while (iter.hasNext()) {
            System.out.print(iter.next());
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        first = (first + 1) % items.length;
        T returnValue = items[first];
        items[first] = null;
        size -= 1;
        return returnValue;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        last = (last - 1 + items.length) % items.length;
        T returnValue = items[last];
        items[last] = null;
        size -= 1;
        return returnValue;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return items[(first + 1 + index) % items.length];
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int count = 0;

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public T next() {
            T returnValue = get(count);
            count += 1;
            return returnValue;
        }
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> tmp = (ArrayDeque<?>) o;
        if (tmp.size != this.size) {
            return false;
        }

        Iterator<T> thisIter = this.iterator();
        Iterator<?> tmpIter = tmp.iterator();

        while (thisIter.hasNext() && tmpIter.hasNext()) {
            T myItem = thisIter.next();
            Object otherItem = tmpIter.next();

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
