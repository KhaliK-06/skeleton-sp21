package deque;

import org.junit.Test;
import java.util.Comparator;
import static org.junit.Assert.*;

/** Performs tests for MaxArrayDeque focusing on the max() methods. */
public class MaxArrayDequeTest {

    // 1. 定义一个用于 Integer 的正序比较器 (标准的从小到大)
    private static class IntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer i1, Integer i2) {
            return i1.compareTo(i2);
        }
    }

    // 2. 定义一个用于 String 的长度比较器
    private static class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.length() - s2.length();
        }
    }

    @Test
    /** 测试使用构造函数中传入的默认 Comparator */
    public void basicMaxTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new IntComparator());

        mad.addLast(10);
        mad.addLast(2);
        mad.addFirst(15);
        mad.addLast(8);
        mad.addFirst(3);

        // 当前队列应该是: [3, 15, 10, 2, 8]
        // 按照 IntComparator (正序)，最大值应该是 15
        assertEquals("Max should be 15", (Integer) 15, mad.max());
    }

    @Test
    /** 测试在调用 max() 时传入一个全新的 Comparator */
    public void customComparatorMaxTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new IntComparator());

        mad.addLast(10);
        mad.addLast(2);
        mad.addLast(15);

        // 使用 Lambda 表达式快速写一个 "倒序" 比较器
        // 逻辑：如果 i2 > i1 返回正数，说明在倒序规则下更小的值反而是 "最大" 的
        Comparator<Integer> reverseComparator = (i1, i2) -> i2.compareTo(i1);

        // 使用默认的比较器，最大值是 15
        assertEquals("Default max should be 15", (Integer) 15, mad.max());

        // 使用倒序比较器，"最大值" 应该是 2
        assertEquals("Reversed max should be 2", (Integer) 2, mad.max(reverseComparator));
    }

    @Test
    /** 测试 String 类型和多种 Comparator 混合使用 */
    public void stringMultipleComparatorTest() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(new StringLengthComparator());

        mad.addLast("apple");
        mad.addLast("watermelon"); // 长度最长
        mad.addLast("zebra");      // 字母表顺序最靠后
        mad.addLast("cat");

        // 默认使用的是 StringLengthComparator，最长的单词是 watermelon
        assertEquals("Longest string should be watermelon", "watermelon", mad.max());

        // 我们再建一个按照字母表顺序 (Alphabetical) 排序的比较器
        Comparator<String> alphabeticalComparator = String::compareTo;

        // 传入字母表比较器，"最大" 的单词应该是 zebra
        assertEquals("Alphabetically last string should be zebra", "zebra", mad.max(alphabeticalComparator));
    }

    @Test
    /** 测试边界情况：空队列必须返回 null */
    public void emptyDequeTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new IntComparator());

        // 刚初始化，是空的
        assertNull("Max of empty deque should be null", mad.max());

        // 加一个又删掉，又变空了
        mad.addFirst(100);
        mad.removeLast();

        // 再次测试两种 max 方法
        assertNull("Max of empty deque should be null", mad.max());

        Comparator<Integer> reverseComparator = (i1, i2) -> i2.compareTo(i1);
        assertNull("Max with custom comparator on empty deque should be null", mad.max(reverseComparator));
    }
}