package cianom.lib;

public class Timing {

    public interface ThrowableRunnable<R> {
        R call() throws Exception;
    }

    public static <R> R time(final String name, final ThrowableRunnable<R> f) throws Exception {
        final long start = System.currentTimeMillis();
        final R result = f.call();
        final long end = System.currentTimeMillis();
        System.out.println(name + " took: " + (end - start) + " millis.");
        return result;
    }

}
