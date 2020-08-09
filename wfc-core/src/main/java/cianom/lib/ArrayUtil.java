package cianom.lib;

public final class ArrayUtil {

    // Prevent construction.
    private ArrayUtil() { }

    /**
     * *<b>NOTE: method mutates the 'arr' input value.</b>*
     * <p>
     * Pick a random index of the 'arr' array, based on the weights on the values.
     * <p>
     * Higher values in 'arr' are given proportionally higher weights (eg. 0.4 is
     * 4 times more likely to be selected than 0.1).
     *
     * @param arr the array of values.
     * @param r a random value between 0 and 1.
     * @return the randomly selected index.
     */
    public static int weightedRandomIndex(double[] arr, double r) {

        double sum = 0;
        for (double v : arr) sum += v;

        for (int j = 0; j < arr.length; j++) arr[j] /= sum;

        double x = 0;
        for (int i = 0; i < arr.length; i++) {
            x += arr[i];
            if (r <= x) return i;
        }

        return 0;
    }

}
