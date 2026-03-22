package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        int[] num = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        int M = 10000;
        AList<Integer> N = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCounts = new AList<Integer>();

        for (int n : num){
            SLList<Integer> test = new SLList<Integer>();
            Stopwatch sw = new Stopwatch();
            helper(n, test);
            for (int i = 0; i < M; i += 1){
                test.getLast();
            }
            double timeInSecond = sw.elapsedTime();
            N.addLast(n);
            times.addLast(timeInSecond);
            opCounts.addLast(M);
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
