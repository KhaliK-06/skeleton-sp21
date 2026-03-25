package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

 @Test
 public void testForSudentArrayDeque() {
     //@source from StuedentArrayDequeLauncher
     StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
     ArrayDequeSolution<Integer> ad1 = new ArrayDequeSolution<>();

     String messageContainer = "";


     for (int i = 0; i < 10; i += 1) {
         double numberBetweenZeroAndOne = StdRandom.uniform();

         if (numberBetweenZeroAndOne < 0.5) {
             sad1.addLast(i);
             ad1.addLast(i);
             messageContainer = "addLast(" + i +")\n";
         } else {
             sad1.addFirst(i);
             ad1.addFirst(i);
             messageContainer = "addFirst(" + i +")\n";
         }

         assertEquals(messageContainer, ad1.size(), sad1.size());
     }

     for (int i = 0; i < 10; i += 1) {
         double numberBetweenZeroAndOne = StdRandom.uniform();

         if (numberBetweenZeroAndOne < 0.5) {
             Integer expected = ad1.removeLast();
             Integer actual = sad1.removeLast();
             messageContainer = "removeFirst(): " + actual + "\n";
             assertEquals(messageContainer, expected, actual);
         } else {
             Integer expected = ad1.removeFirst();
             Integer actual = sad1.removeFirst();
             messageContainer = "removeFirst(): " + actual + "\n";
             assertEquals(messageContainer, expected, actual);
         }

         assertEquals(messageContainer, ad1.size(), sad1.size());
     }

 }

}
