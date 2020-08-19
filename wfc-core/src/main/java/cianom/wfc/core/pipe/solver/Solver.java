package cianom.wfc.core.pipe.solver;


import cianom.lib.ArrayUtil;
import cianom.lib.Boundary;
import cianom.lib.IntPoint;
import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.Pipe;
import cianom.wfc.core.api.PatternSet;

import java.lang.reflect.Array;
import java.util.*;


public class Solver<T> implements Pipe<PatternSet<T>, Solver.Solution<T>> {

    private final ModelConfig conf;

    protected final Map<Boundary, int[][]> propagator;
    protected int[] observed;
    protected T[] observedOut;
    private Stack<IntPoint> stack;
    private PositionState[] positions;

    public Solver(final ModelConfig conf) {
        this.conf = conf;
        this.propagator = new HashMap<>();
    }

    protected boolean onBoundary(final PatternSet<T> in, int x, int y) {
        return (
                !this.conf.periodicOutput &&
                        (x + in.getN() > conf.outWidth || y + in.getN() > conf.outHeight || x < 0 || y < 0)
        );
    }


    void init(final PatternSet<T> in) {

        // Build propagator
        final int patternCount = in.getPatternCount();
        propagator.clear();
        for (final Boundary b : Boundary.values()) {
            final int[][] xx = new int[patternCount][];
            this.propagator.put(b, xx);
            for (int t = 0; t < patternCount; t++) {
                final List<Integer> list = new ArrayList<>();
                for (int t2 = 0; t2 < patternCount; t2++) {
                    if (agrees(in.getPatternByIndex(t), in.getPatternByIndex(t2), b, in.getN())) {
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

        this.positions = new PositionState[conf.outWidth * conf.outHeight];
        for (int i = 0; i < positions.length; i++) {
            this.positions[i] = PositionState.create(in);
        }

        this.stack = new Stack<>();
    }

    private Integer findLowestPositionEntropy(final PatternSet<T> in, final Random random) {
        int argmin = -1;
        double min = 1e+3;
        for (int i = 0; i < this.positions.length; i++) {
            if (this.onBoundary(in, i % conf.outWidth, i / conf.outWidth)) continue;

            int amount = this.positions[i].potentialPatterns;
            if (amount == 0) return null;

            double entropy = this.positions[i].entropy;

            if (amount > 1 && entropy <= min) {
                double noise = 1e-6 * random.nextDouble();
                if (entropy + noise < min) {
                    min = entropy + noise;
                    argmin = i;
                }
            }
        }
        return argmin;
    }

    ObserveResult observe(final PatternSet<T> in, final Random random) {
        final int T = in.getPatternCount();


        // Find position with lowest entropy.
        final Integer argmin = findLowestPositionEntropy(in, random);

        if (argmin == null) {
            return ObserveResult.FAILED;
        }
        // If all cells are at entropy 0, processing is complete:
        else if (argmin == -1) {
            // Build collapsed observations for completion
            this.observed = new int[conf.outWidth * conf.outHeight];
            this.observedOut = (T[]) Array.newInstance(in.getValueClass(), conf.outWidth * conf.outHeight);
            for (int i = 0; i < this.positions.length; i++) {
                for (int t = 0; t < T; t++) {
                    if (this.positions[i].wave[t]) {
                        final int x = i % conf.outWidth;
                        final int y = i / conf.outWidth;
                        int dx = x < conf.outWidth - in.getN() + 1 ? 0 : in.getN() - 1;
                        int dy = y < conf.outHeight - in.getN() + 1 ? 0 : in.getN() - 1;
                        int idx = dx + dy * in.getN();
                        final Integer xxxx = in.getPatternByIndex(t).value(idx);
                        final T v = in.getDistinctValues().get(xxxx);
                        observedOut[i] = v;

                        this.observed[i] = t;
                        break;
                    }
                }
            }
            return ObserveResult.DONE;
        } else {
            // Choose a pattern by a random sample, weighted by the pattern frequency in the source data.
            double[] distribution = new double[T];
            for (int t = 0; t < T; t++) {
                distribution[t] = this.positions[argmin].wave[t] ? in.getPatternByIndex(t).getFrequency() : 0;
            }

            final int r = ArrayUtil.weightedRandomIndex(distribution, random.nextDouble());

            boolean[] w = this.positions[argmin].wave;
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
        this.positions[i].wave[t] = false;

        final int[] comp = this.positions[i].compatible[t];
        for (int d = 0; d < 4; d++) comp[d] = 0;
        stack.push(new IntPoint(i, t));

        this.positions[i].ban(in.getPatternByIndex(t));
    }

    protected void propagate(final PatternSet<T> in) {
        while (!this.stack.isEmpty()) {
            IntPoint e1 = this.stack.pop();

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
                int[][] compat = this.positions[i2].compatible;

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
        if (this.positions == null) this.init(in);

        this.clear(in);
        final Random random = new Random(conf.seed);

        int runs = 0;
        for (; runs < conf.limit || conf.limit == 0; runs++) {
            ObserveResult result = this.observe(in, random);
            switch (result) {
                case DONE:
                    return new Solution<>(conf.outWidth, conf.outHeight, observedOut, in, runs);
                case NOT_DONE:
                    this.propagate(in);
                    break;
                case FAILED:
                    break;
            }
        }

        return new Solution<>(conf.outWidth, conf.outHeight, observedOut, in, runs);
    }

    protected void clear(final PatternSet<T> in) {
        final int patternCount = in.getPatternCount();
        final int ground = in.computeGround();

        for (int i = 0; i < this.positions.length; i++) {
            for (int t = 0; t < patternCount; t++) {
                this.positions[i].wave[t] = true;
                for (int d = 0; d < 4; d++) {
                    final Boundary b = Boundary.values()[d];
                    this.positions[i].compatible[t][d] =
                            this.propagator.get(b.opposite())[t].length;
                }
            }

            // Overlapping clear
            if (ground != 0) {
                for (int x = 0; x < conf.outWidth; x++) {
                    for (int t = 0; t < patternCount; t++) {
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

    Boolean agrees(final Pattern p1, final Pattern p2, final Boundary b, final int N) {
        final int xmin = Math.max(b.x, 0);
        final int xmax = b.x < 0 ? b.x + N : N;
        final int ymin = Math.max(b.y, 0);
        final int ymax = b.y < 0 ? b.y + N : N;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (!Objects.equals(p1.getData()[x + N * y], p2.getData()[x - b.x + N * (y - b.y)])) return false;
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
        public final int runCount;

        public Solution(int width, int height, T[] observed, PatternSet<T> in, final int runCount) {
            this.width = width;
            this.height = height;
            this.observed = observed;
            this.in = in;
            this.runCount = runCount;
        }
    }

    public static final class ModelConfig {
        public final int limit;
        public final int seed;
        public final int outWidth;
        public final int outHeight;
        public final boolean periodicOutput;

        public ModelConfig(int limit, int seed, int outWidth, int outHeight, boolean periodicOutput) {
            this.limit = limit;
            this.seed = seed;
            this.outWidth = outWidth;
            this.outHeight = outHeight;
            this.periodicOutput = periodicOutput;
        }
    }

}
