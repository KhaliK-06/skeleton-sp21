package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> BA = new BuggyAList<Integer>();
        AListNoResizing<Integer> A = new AListNoResizing<Integer>();
        BA.addLast(1);
        BA.addLast(2);
        BA.addLast(3);
        A.addLast(1);
        A.addLast(2);
        A.addLast(3);
        BA.removeLast();
        BA.removeLast();
        A.removeLast();
        A.removeLast();

        assertEquals(A.getLast(), BA.getLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L0 = new BuggyAList<Integer>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
                L0.addLast(randVal);
                System.out.println("addLast(Buggy)(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                System.out.println("size: " + size);
                int size0 = L0.size();
                System.out.println("size(Buggy): " + size0);
                assertEquals(size, size0);
            } else if (operationNumber == 2 && L.size() > 0) {
                // getLast
                int Last = L.getLast();
                System.out.println("getLast: " + Last);
                int Last0 = L0.getLast();
                System.out.println("getLast(Buggy: " + Last0);
                assertEquals(Last, Last0);
            } else if (operationNumber == 3 && L.size() > 0) {
                // removeLast
                int Last = L.getLast();
                L.removeLast();
                System.out.println("removeLast: " + Last);
                int Last0 = L0.getLast();
                L0.removeLast();
                System.out.println("removeLast(Buggy): " + Last0);
                assertEquals(Last, Last0);
            }
        }
    }
}
