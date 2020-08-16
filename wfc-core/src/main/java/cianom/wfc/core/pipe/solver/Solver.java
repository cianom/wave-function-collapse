package cianom.wfc.core.pipe.solver;


import cianom.lib.ArrayUtil;
import cianom.lib.Boundary;
import cianom.lib.IntPoint;
import cianom.wfc.core.api.Pipe;
import cianom.wfc.core.api.PatternSet;

import java.lang.reflect.Array;
import java.util.*;


public class Solver<T> implements Pipe<PatternSet<T>, Solver.Solution<T>> {

    private final ModelConfig conf;
    protected boolean[][] wave;
    protected final Map<Boundary, int[][]> propagator;
    int[][][] compatible;
    protected int[] observed;
    protected T[] observedOut;

    IntPoint[] stack;
    int stacksize;

    protected boolean periodic;
    double[] weightLogWeights;

    int[] sumsOfOnes;
    double sumOfWeights;
    double sumOfWeightLogWeights;
    double startingEntropy;
    double[] sumsOfWeights;
    double[] sumsOfWeightLogWeights;
    double[] entropies;

    public Solver(final ModelConfig conf) {
        this.conf = conf;
        this.propagator = new HashMap<>();
    }

    protected boolean onBoundary(final PatternSet<T> in, int x, int y) {
        return (
                !this.periodic &&
                        (x + in.getN() > conf.outWidth || y + in.getN() > conf.outHeight || x < 0 || y < 0)
        );

    }


    void init(final PatternSet<T> in) {

        // Build propagator
        final int T = in.getT();
        propagator.clear();
        final Integer[][] patterns = in.getPatterns();
        for (final Boundary b : Boundary.values()) {
            final int[][] xx = new int[T][];
            this.propagator.put(b, xx);
            for (int t = 0; t < T; t++) {
                final List<Integer> list = new ArrayList<>();
                for (int t2 = 0; t2 < T; t2++) {
                    if (agrees(patterns[t], patterns[t2], b, in.getN())) {
                        list.add(t2);
                    }
                }
                xx[t] = new int[list.size()];
                for (int c = 0; c < list.size(); c++) {
                    xx[t][c] = list.get(c);
                }
            }
        }
        // End build propagator

        this.wave = new boolean[conf.outWidth * conf.outHeight][];
        this.compatible = new int[this.wave.length][][];
        for (int i = 0; i < wave.length; i++) {
            this.wave[i] = new boolean[T];
            this.compatible[i] = new int[T][];
            for (int t = 0; t < T; t++) this.compatible[i][t] = new int[4];
        }

        this.weightLogWeights = new double[T];
        this.sumOfWeights = 0;
        this.sumOfWeightLogWeights = 0;

        for (int t = 0; t < T; t++) {
            final double weight = in.getWeight(t);
            this.weightLogWeights[t] = weight * Math.log(weight);
            this.sumOfWeights += weight;
            this.sumOfWeightLogWeights += this.weightLogWeights[t];
        }

        this.startingEntropy =
                Math.log(this.sumOfWeights) - (this.sumOfWeightLogWeights / this.sumOfWeights);

        this.sumsOfOnes = new int[conf.outWidth * conf.outHeight];
        this.sumsOfWeights = new double[conf.outWidth * conf.outHeight];
        this.sumsOfWeightLogWeights = new double[conf.outWidth * conf.outHeight];
        this.entropies = new double[conf.outWidth * conf.outHeight];

        this.stack = new IntPoint[this.wave.length * T];
        this.stacksize = 0;
    }

    ObserveResult observe(final PatternSet<T> in, final Random random) {
        final int T = in.getT();
        double min = 1e+3;
        int argmin = -1;

        for (int i = 0; i < this.wave.length; i++) {
            if (this.onBoundary(in, i % conf.outWidth, i / conf.outWidth)) continue;

            int amount = this.sumsOfOnes[i];
            if (amount == 0) return ObserveResult.FAILED;

            double entropy = this.entropies[i];

            if (amount > 1 && entropy <= min) {
                double noise = 1e-6 * random.nextDouble();
                if (entropy + noise < min) {
                    min = entropy + noise;
                    argmin = i;
                }
            }
        }

        // If all cells are at entropy 0, processing is complete:
        if (argmin == -1) {
            // Build collapsed observations for completion
            this.observed = new int[conf.outWidth * conf.outHeight];
            this.observedOut = (T[]) Array.newInstance(in.gettClass(), conf.outWidth * conf.outHeight);
            for (int i = 0; i < this.wave.length; i++) {
                for (int t = 0; t < T; t++) {
                    if (this.wave[i][t]) {
                        final int x = i % conf.outWidth;
                        final int y = i / conf.outWidth;
                        int dx = x < conf.outWidth - in.getN() + 1 ? 0 : in.getN() - 1;
                        int dy = y < conf.outHeight - in.getN() + 1 ? 0 : in.getN() - 1;
                        int idx = dx + dy * in.getN();
//            int t = this.observed[x - dx + (y - dy) * this.targetWidth];
                        final Integer xxxx = in.getPatterns()[t][idx];
                        final T v = in.getDistinctValues().get(xxxx);
                        observedOut[i] = v;

                        this.observed[i] = t;
                        break;
                    }
                }
            }
            return ObserveResult.DONE;
        } else {
            // Choose a pattern by a random sample, weighted by the pattern frequency in the source data
            double[] distribution = new double[T];
            for (int t = 0; t < T; t++) {
                distribution[t] = this.wave[argmin][t] ? in.getWeight(t) : 0;
            }

            final int r = ArrayUtil.weightedRandomIndex(distribution, random.nextDouble());

            boolean[] w = this.wave[argmin];
            for (int t = 0; t < T; t++) {
                if (w[t] != (t == r)) {
                    // Set the boolean array in this cell to false, except for the chosen pattern
                    this.ban(in, argmin, t);
                }
            }

            return ObserveResult.NOT_DONE;
        }
    }

    protected void ban(final PatternSet<T> in, int i, int t) {
        this.wave[i][t] = false;

        int[] comp = this.compatible[i][t];
        for (int d = 0; d < 4; d++) comp[d] = 0;
        this.stack[this.stacksize] = new IntPoint(i, t);
        this.stacksize++;

        this.sumsOfOnes[i] -= 1;
        this.sumsOfWeights[i] -= in.getWeight(t);
        this.sumsOfWeightLogWeights[i] -= this.weightLogWeights[t];

        double sum = this.sumsOfWeights[i];
        this.entropies[i] = Math.log(sum) - this.sumsOfWeightLogWeights[i] / sum;
    }

    protected void propagate(final PatternSet<T> in) {
        while (this.stacksize > 0) {
            IntPoint e1 = this.stack[this.stacksize - 1];
            this.stacksize--;

            int i1 = e1.getFirst();
            int x1 = i1 % conf.outWidth;
            int y1 = i1 / conf.outWidth;

            for (int d = 0; d < 4; d++) {
                final Boundary b = Boundary.values()[d];
                int x2 = x1 + b.x, y2 = y1 + b.y;

                if (this.onBoundary(in, x2, y2)) continue;

                if (x2 < 0) x2 += conf.outWidth;
                else if (x2 >= conf.outWidth) x2 -= conf.outWidth;
                if (y2 < 0) y2 += conf.outHeight;
                else if (y2 >= conf.outHeight) y2 -= conf.outHeight;

                int i2 = x2 + y2 * conf.outWidth;
                int[] p = this.propagator.get(b)[e1.getSecond()];
                int[][] compat = this.compatible[i2];

                for (final int t2 : p) {
                    int[] comp = compat[t2];

                    comp[d]--;

                    if (comp[d] == 0) this.ban(in, i2, t2);
                }
            }
        }
    }

    @Override
    public Solution<T> run(final PatternSet<T> in) {
        if (this.wave == null) this.init(in);

        this.clear(in);
        final Random random = new Random(conf.seed);

        // TODO do retries even make sense with a seeded generator?
        for (int l = 0; l < conf.limit || conf.limit == 0; l++) {
            ObserveResult result = this.observe(in, random);
            switch (result) {
                case DONE:
                    return new Solution<>(conf.outWidth, conf.outHeight, observedOut, in);
                case NOT_DONE:
                    this.propagate(in);
                    break;
                case FAILED:
                    break;
            }
        }

        return new Solution<>(conf.outWidth, conf.outHeight, observedOut, in);
    }

    protected void clear(final PatternSet<T> in) {
        final int T = in.getT();
        final int ground = in.computeGround();

        for (int i = 0; i < this.wave.length; i++) {
            for (int t = 0; t < T; t++) {
                this.wave[i][t] = true;
                for (int d = 0; d < 4; d++) {
                    final Boundary b = Boundary.values()[d];
                    this.compatible[i][t][d] =
                            this.propagator.get(b.opposite())[t].length;
                }
            }

            this.sumsOfOnes[i] = in.getT();
            this.sumsOfWeights[i] = this.sumOfWeights;
            this.sumsOfWeightLogWeights[i] = this.sumOfWeightLogWeights;
            this.entropies[i] = this.startingEntropy;

            // Overlapping clear
            if (ground != 0) {
                for (int x = 0; x < conf.outWidth; x++) {
                    for (int t = 0; t < T; t++) {
                        if (t != ground) {
                            this.ban(in, x + (conf.outHeight - 1) * conf.outWidth, t);
                        }
                    }

                    for (int y = 0; y < conf.outHeight - 1; y++) {
                        this.ban(in, x + y * conf.outWidth, ground);
                    }
                }

                this.propagate(in);
            }
        }
    }

    Boolean agrees(final Integer[] patterns1, final Integer[] patterns2, final Boundary b, final int N) {
        final int xmin = Math.max(b.x, 0);
        final int xmax = b.x < 0 ? b.x + N : N;
        final int ymin = Math.max(b.y, 0);
        final int ymax = b.y < 0 ? b.y + N : N;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (!Objects.equals(patterns1[x + N * y], patterns2[x - b.x + N * (y - b.y)])) return false;
            }
        }
        return true;
    }

    public enum ObserveResult {
        DONE, NOT_DONE, FAILED
    }

    public static final class Solution<T> {
        public final int width;
        public final int height;
        public final T[] observed;
        public final PatternSet<T> in;

        public Solution(int width, int height, T[] observed, PatternSet<T> in) {
            this.width = width;
            this.height = height;
            this.observed = observed;
            this.in = in;
        }
    }

    public static final class ModelConfig {
        public final int limit;
        public final int seed;
        public final int outWidth;
        public final int outHeight;

        public ModelConfig(int limit, int seed, int outWidth, int outHeight) {
            this.limit = limit;
            this.seed = seed;
            this.outWidth = outWidth;
            this.outHeight = outHeight;
        }
    }

}
