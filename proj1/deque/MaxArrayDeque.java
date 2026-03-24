package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        cmp = c;
    }

    public T max() {
        if (super.isEmpty()) {
            return null;
        }
        T max = this.get(0);
        for (int i = 1; i < super.size(); i += 1) {
            if (cmp.compare(this.get(i), max) > 0) {
                max = this.get(i);
            }
        }
        return max;
    }

    public T max(Comparator<T> c) {
        if (super.isEmpty()) {
            return null;
        }
        T max = this.get(0);
        for (int i = 1; i < super.size(); i += 1) {
            if (c.compare(this.get(i), max) > 0) {
                max = this.get(i);
            }
        }
        return max;
    }

}
