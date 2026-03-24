package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    private static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final int NUM_STRINGS = 37;

    public static void main(String[] args) {
        GuitarString[] strings = new GuitarString[NUM_STRINGS];
        for (int i = 0; i < NUM_STRINGS; i++) {
            double frequency = 110.0 * Math.pow(2.0, i / 12.0);
            strings[i] = new GuitarString(frequency);
        }

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int index = KEYBOARD.indexOf(key);
                if (index >= 0) {
                    strings[index].pluck();
                }
            }

            double sample = 0.0;
            for (int i = 0; i < NUM_STRINGS; i++) {
                sample += strings[i].sample();
            }

            StdAudio.play(sample);

            for (int i = 0; i < NUM_STRINGS; i++) {
                strings[i].tic();
            }
        }
    }
}