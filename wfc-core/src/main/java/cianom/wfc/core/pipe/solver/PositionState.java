package cianom.wfc.core.pipe.solver;

import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;

class PositionState {
    boolean[] wave;
    double entropy;
    double sumOfWeights;
    double sumOfWeightLogWeights;
    int potentialPatterns;
    // patternIndex, boundary
    int[][] compatible;

    private PositionState(boolean[] wave, double entropy, double sumOfWeights, double sumOfWeightLogWeights, int potentialPatterns, int[][] compatible) {
        this.wave = wave;
        this.entropy = entropy;
        this.sumOfWeights = sumOfWeights;
        this.sumOfWeightLogWeights = sumOfWeightLogWeights;
        this.potentialPatterns = potentialPatterns;
        this.compatible = compatible;
    }

    static PositionState create(final PatternSet<?> in) {
        final int patternCount = in.getPatternCount();

        final int[][] compatible = new int[patternCount][];
        for (int t = 0; t < patternCount; t++) compatible[t] = new int[4];

        final boolean[] wave = new boolean[patternCount];

        double sumOfWeight = 0D;
        for (int t = 0; t < patternCount; t++) {
            final double weight = in.getPatternByIndex(t).getFrequency();
            sumOfWeight += weight;
        }

        final int sumOfOnes = in.getPatternCount();

        final double sumOfWeightLogWeights = in.computeSumOfFrequenciesLogFrequencies();

        final double startingEntropy =
                Math.log(in.computeSumOfFrequencies()) - (in.computeSumOfFrequenciesLogFrequencies() / in.computeSumOfFrequencies());

        return new PositionState(wave, startingEntropy, sumOfWeight, sumOfWeightLogWeights, sumOfOnes, compatible);
    }

    public void ban(final Pattern p) {

        this.potentialPatterns--;
        this.sumOfWeights -= p.getFrequency();
        this.sumOfWeightLogWeights -= p.getFrequencyLogFrequency();

        double sum = this.sumOfWeights;
        this.entropy = Math.log(sum) - this.sumOfWeightLogWeights / sum;
    }
}
