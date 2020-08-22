package cianom.wfc.core.pipe.solver;

import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;

class PositionState {

    final int index;
    boolean[] wave;
    double entropy;
    double sumOfFrequencies;
    double sumOfFrequencyLogFrequencies;
    int potentialPatterns;
    // patternIndex, boundary
    int[][] compatible;

    private PositionState(final int index, boolean[] wave, double entropy, double sumOfFrequencies, double sumOfFrequencyLogFrequencies, int potentialPatterns, int[][] compatible) {
        this.index = index;
        this.wave = wave;
        this.entropy = entropy;
        this.sumOfFrequencies = sumOfFrequencies;
        this.sumOfFrequencyLogFrequencies = sumOfFrequencyLogFrequencies;
        this.potentialPatterns = potentialPatterns;
        this.compatible = compatible;
    }


    static PositionState create(final int index, final PatternSet<?> in) {
        final int patternCount = in.getPatternCount();

        final int[][] compatible = new int[patternCount][];
        for (int t = 0; t < patternCount; t++) compatible[t] = new int[4];

        final boolean[] wave = new boolean[patternCount];

        double sumOfFrequencies = 0D;
        for (int t = 0; t < patternCount; t++) {
            final double weight = in.getPatternByIndex(t).getFrequency();
            sumOfFrequencies += weight;
        }

        final int sumOfOnes = in.getPatternCount();

        final double sumOfFrequencyLogFrequencies = in.computeSumOfFrequenciesLogFrequencies();

        final double startingEntropy =
                Math.log(in.computeSumOfFrequencies()) - (in.computeSumOfFrequenciesLogFrequencies() / in.computeSumOfFrequencies());

        return new PositionState(index, wave, startingEntropy, sumOfFrequencies, sumOfFrequencyLogFrequencies, sumOfOnes, compatible);
    }

    public void ban(final Pattern p) {

        this.potentialPatterns--;
        this.sumOfFrequencies -= p.getFrequency();
        this.sumOfFrequencyLogFrequencies -= p.getFrequencyLogFrequency();

        this.entropy = Math.log(this.sumOfFrequencies) - this.sumOfFrequencyLogFrequencies / this.sumOfFrequencies;
    }

    public Pattern collapse(final PatternSet<?> in) {
        for (final Pattern p : in.getPatterns()) {
            if (this.wave[p.getIndex()]) {
                return in.getPatternByIndex(p.getIndex());
            }
        }
        throw new IllegalStateException("Un-collapsable position at index " + index);

    }
}
