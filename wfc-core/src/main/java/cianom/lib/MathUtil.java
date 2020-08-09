package cianom.lib;

public final class MathUtil {

    // Prevent construction.
    private MathUtil() { }


    public static long pow(final long base, final int power) {
        long product = 1;
        for (int i = 0; i < power; i++) product *= base;
        return product;
    }

}
