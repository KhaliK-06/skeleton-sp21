package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int[] num = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 10000000};
        AList<Integer> N = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCounts = new AList<Integer>();

        for (int n : num){
            SLList<Integer> test = new SLList<Integer>();
            helper(n, test);
            Stopwatch sw = new Stopwatch();

            double timeInSecond = sw.elapsedTime();
            N.addLast(n);
            times.addLast(timeInSecond);
            opCounts.addLast(n);
        }

        printTimingTable(N, times, opCounts);

    }

    public static void helper(int n, SLList<Integer> A) {
        int i = 0;
        while(i < n){
            A.addLast(1);
            i += 1;
        }
    }

}
