package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {

        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        System.out.println("Make sure to uncomment the lines below (and delete this print statement).");
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    /** 对应报错 1：Test Iterator next values (检测是否跳过了 Sentinel) */
    public void testBasicIteration() {
        LinkedListDeque<String> lld = new LinkedListDeque<>();
        lld.addLast("Boren");
        lld.addLast("Josh");
        lld.addLast("Hug");

        Iterator<String> iter = lld.iterator();

        assertTrue("迭代器应该有下一个元素", iter.hasNext());
        // 如果这里报错并预期拿到 null，说明你的指针没有跳过 Sentinel！
        assertEquals("第一个拿出的元素必须是真正的第一个值", "Boren", iter.next());

        assertTrue(iter.hasNext());
        assertEquals("Josh", iter.next());

        assertTrue(iter.hasNext());
        assertEquals("Hug", iter.next());

        // 遍历完 3 个元素后，hasNext 必须返回 false
        assertFalse("遍历结束后不应该再有元素", iter.hasNext());
    }

    @Test
    /** 对应报错 2：Test hasNext on empty iterator (检测空链表的情况) */
    public void testEmptyIterator() {
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        Iterator<Integer> iter = lld.iterator();

        // 对于刚初始化出来的空链表，一开始就应该是 false
        assertFalse("空链表的迭代器 hasNext 必须直接返回 false", iter.hasNext());
    }

    @Test
    /** 对应报错 3 & 4：Test with two/many iterators (检测迭代器指针是否互相独立) */
    public void testMultipleIterators() {
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        lld.addLast(10);
        lld.addLast(20);
        lld.addLast(30);

        // 我们同时召唤出两个迭代器！
        Iterator<Integer> iter1 = lld.iterator();
        Iterator<Integer> iter2 = lld.iterator();

        // 让 iter1 先走一步
        assertTrue(iter1.hasNext());
        assertEquals("iter1 应该拿到第一个元素 10", (Integer) 10, iter1.next());

        // 此时 iter2 才开始走第一步。如果它拿到了 20，说明它被 iter1 干扰了（状态共享 Bug！）
        assertTrue(iter2.hasNext());
        assertEquals("iter2 的起点必须不受 iter1 影响，依然是 10", (Integer) 10, iter2.next());

        // 两人交替往前走
        assertEquals("iter1 拿第二个元素", (Integer) 20, iter1.next());
        assertEquals("iter2 拿第二个元素", (Integer) 20, iter2.next());

        assertEquals("iter1 拿第三个元素", (Integer) 30, iter1.next());
        assertEquals("iter2 拿第三个元素", (Integer) 30, iter2.next());

        // 两人同时到达终点
        assertFalse("iter1 应该结束了", iter1.hasNext());
        assertFalse("iter2 应该结束了", iter2.hasNext());
    }
}
