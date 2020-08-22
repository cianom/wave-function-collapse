package cianom.wfc.core.pipe.solver;


import cianom.lib.ArrayUtil;
import cianom.lib.Boundary;
import cianom.lib.Pair;
import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;
import cianom.wfc.core.api.Pipe;

import java.util.List;
import java.util.Random;
import java.util.Stack;


public class Solver<T> implements Pipe<PatternSet<T>, Solver.Solution<T>> {

    private final ModelConfig conf;

    public Solver(final ModelConfig conf) {
        this.conf = conf;
    }

    protected boolean onBoundary(final PatternSet<T> in, int x, int y) {
        return (
                !this.conf.periodicOutput &&
                        (x + in.getN() > conf.outWidth || y + in.getN() > conf.outHeight || x < 0 || y < 0)
        );
    }

    private Integer findLowestPositionEntropy(final PatternSet<T> in, PositionState[] positions, final Random random) {
        int minEntropyPosition = -1;
        double currentMin = 1e+3;
        for (int i = 0; i < positions.length; i++) {
            final PositionState pos = positions[i];
            if (this.onBoundary(in, i % conf.outWidth, i / conf.outWidth)) continue;

            int amount = pos.potentialPatterns;
            if (amount == 0) return null;

            double entropy = pos.entropy;

            if (amount > 1 && entropy <= currentMin) {
                double noise = 1e-6 * random.nextDouble();
                if (entropy + noise < currentMin) {
                    currentMin = entropy + noise;
                    minEntropyPosition = pos.index;
                }
            }
        }
        return minEntropyPosition;
    }

    @SuppressWarnings("unchecked")
    Pair<ObserveResult, Collapsed<T>[]> observe(final PatternSet<T> in, final PositionState[] positions, final Stack<PatternAtPosition> stack, final Random random) {
        final int T = in.getPatternCount();

        // Find position with lowest entropy.
        final Integer minEntropyPosition = findLowestPositionEntropy(in, positions, random);

        if (minEntropyPosition == null) {
            return new Pair<>(ObserveResult.FAILED, null);
        }
        // If all cells are at entropy 0, processing is complete:
        else if (minEntropyPosition == -1) {
            // Build collapsed observations for completion
            final Collapsed<T>[] observed = new Collapsed[conf.outWidth * conf.outHeight];
            for (int i = 0; i < positions.length; i++) {
                final Pattern collapsedPattern = positions[i].collapse(in);
                writeObserved(observed, in, i, collapsedPattern);
            }
            return new Pair<>(ObserveResult.DONE, observed);
        } else {
            // Choose a pattern by a random sample, weighted by the pattern frequency in the source data.
            double[] distribution = new double[T];
            for (int t = 0; t < T; t++) {
                distribution[t] = positions[minEntropyPosition].wave[t] ? in.getPatternByIndex(t).getFrequency() : 0;
            }

            final int r = ArrayUtil.weightedRandomIndex(distribution, random.nextDouble());

            boolean[] w = positions[minEntropyPosition].wave;
            for (final Pattern p : in.getPatterns()) {
                if (w[p.getIndex()] != (p.getIndex() == r)) {
                    // Set the boolean array in this cell to false, except for the chosen pattern
                    this.ban(positions, stack, minEntropyPosition, p);
                }
            }

            return new Pair<>(ObserveResult.NOT_DONE, null);
        }
    }

    private void writeObserved(final Collapsed<T>[] observed, final PatternSet<T> in, int i, Pattern collapsed) {
        final int x = i % conf.outWidth;
        final int y = i / conf.outWidth;
        final int xAdjust = x < conf.outWidth - in.getN() + 1 ? 0 : in.getN() - 1;
        final int yAdjust = y < conf.outHeight - in.getN() + 1 ? 0 : in.getN() - 1;
        final boolean onEdge = (xAdjust > 0 || yAdjust > 0);

        // If we're on the edge, we want to select the adjacent pattern but with .
        Pattern selectedPattern = collapsed;
        if (onEdge) {
            final Collapsed<T> tAdjust = observed[x - xAdjust + (y - yAdjust) * conf.outWidth];
            selectedPattern = tAdjust.pattern;
        }

        final int idx = xAdjust + yAdjust * in.getN();
        final Integer valueIndex = selectedPattern.value(idx);
        final T v = in.getDistinctValues().get(valueIndex);

        observed[i] = new Collapsed<>(i, idx, selectedPattern, v);
    }

    protected void ban(final PositionState[] positions, final Stack<PatternAtPosition> propagationStack, int i, final Pattern pattern) {
        positions[i].wave[pattern.getIndex()] = false;

        final int[] comp = positions[i].compatible[pattern.getIndex()];
        for (int d = 0; d < 4; d++) comp[d] = 0;
        propagationStack.push(new PatternAtPosition(i, pattern));

        positions[i].ban(pattern);
    }

    protected void propagate(final PatternSet<T> in, final PositionState[] positions, final Stack<PatternAtPosition> stack) {
        while (!stack.isEmpty()) {
            PatternAtPosition pp = stack.pop();

            int i1 = pp.getPosition();
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
                final List<Pattern> compatabilities = pp.getPattern().getCompatabilities(b);
                int[][] compat = positions[i2].compatible;

                for (final Pattern t2 : compatabilities) {
                    final int[] comp = compat[t2.getIndex()];

                    comp[d]--;

                    if (comp[d] == 0) this.ban(positions, stack, i2, t2);
                }
            }
        }
    }

    @Override
    public Solution<T> run(final PatternSet<T> in) {

        final PositionState[] positions = PositionState.createArray(conf.outWidth * conf.outHeight, in);
        final Stack<PatternAtPosition> stack = new Stack<>();

        this.clear(in, positions, stack);
        final Random random = new Random(conf.seed);

        int runs = 0;
        for (; runs < conf.limit || conf.limit == 0; runs++) {
            Pair<ObserveResult, Collapsed<T>[]> result = this.observe(in, positions, stack, random);
            switch (result.one) {
                case DONE:
                    return new Solution<>(conf.outWidth, conf.outHeight, result.two, in, runs);
                case NOT_DONE:
                    this.propagate(in, positions, stack);
                    break;
                case FAILED:
                    new Solution<>(conf.outWidth, conf.outHeight, null, in, runs);
            }
            if (runs % 10_000 == 0) System.out.println("iteration " + runs);
        }

        return new Solution<>(conf.outWidth, conf.outHeight, null, in, runs);
    }

    protected void clear(final PatternSet<T> in, final PositionState[] positions, final Stack<PatternAtPosition> propagationStack) {
        final int patternCount = in.getPatternCount();
        final Pattern groundPattern = in.computeGroundPattern();

        for (final PositionState pos : positions) {

            // Reset compatibilities.
            for (final Pattern p : in.getPatterns()) {
                pos.wave[p.getIndex()] = true;

                for (int d = 0; d < 4; d++) {
                    final Boundary b = Boundary.values()[d];
                    pos.compatible[p.getIndex()][d] = p.getCompatabilities(b.opposite()).size();
                }
            }

            // Overlapping clear
            if (groundPattern != null) {
                for (int x = 0; x < conf.outWidth; x++) {
                    for (int t = 0; t < patternCount; t++) {
                        if (t != groundPattern.getIndex()) {
                            this.ban(positions, propagationStack, x + (conf.outHeight - 1) * conf.outWidth, in.getPatternByIndex(t));
                        }
                    }

                    for (int y = 0; y < conf.outHeight - 1; y++) {
                        this.ban(positions, propagationStack, x + y * conf.outWidth, groundPattern);
                    }
                }

                this.propagate(in, positions, propagationStack);
            }
        }
    }


    public enum ObserveResult {
        DONE, NOT_DONE, FAILED
    }

    public static final class Solution<T> {
        public final int width;
        public final int height;
        public final Collapsed<T>[] observed;
        public final PatternSet<T> in;
        public final int runCount;

        public Solution(int width, int height, Collapsed<T>[] observed, PatternSet<T> in, final int runCount) {
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
