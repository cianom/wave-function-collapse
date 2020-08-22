package cianom.wfc.core.pipe.solver;

import cianom.wfc.core.api.PatternSet;

import java.util.Random;

public class PositionGrid {

    public final PositionState[] positions;
    private final int width;
    private final int height;
    private final boolean periodicOutput;

    private PositionGrid(final PositionState[] positions, int width, final int height, final boolean periodicOutput) {
        this.positions = positions;
        this.width = width;
        this.height = height;
        this.periodicOutput = periodicOutput;
    }

    boolean onBoundary(final int N, int x, int y) {
        return (!periodicOutput &&
                (x + N > width || y + N > height || x < 0 || y < 0)
        );
    }

     Integer findLowestPositionEntropy(final PatternSet<?> in, final Random random) {
        int minEntropyPosition = -1;
        double currentMin = 1e+3;
        for (int i = 0; i < positions.length; i++) {
            final PositionState pos = positions[i];
            if (onBoundary(in.getN(), i % width, i / width)) continue;

            final int amount = pos.potentialPatterns;
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

    public PositionState get(final int pos) {
        return this.positions[pos];
    }

    public int length() {
        return positions.length;
    }

    static PositionGrid create(final int width, final int height, final boolean periodicOutput, final PatternSet<?> in) {
        final PositionState[] positions = new PositionState[width * height];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = PositionState.create(i, in);
        }
        return new PositionGrid(positions, width, height, periodicOutput);
    }

}
