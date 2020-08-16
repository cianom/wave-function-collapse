package cianom.wfc.core.api;


import java.util.List;

public class PatternSet<T> {


    /**
     * The size of the pattern sample.
     */
    private final int N;
    private final int nominalGround;
    private final int width;
    private final int height;
    private final Integer[][] sample;
    private final List<T> distinctValues;
    private final Class<T> tClass;


    private final Integer[][] patterns;
    private final double[] weightByIndex;
    private final List<Integer> ordering;

    public PatternSet(final int n,
                      final int nominalGround,
                      final int width,
                      final int height,
                      final Integer[][] sample,
                      final List<T> distinctValues,
                      final Class<T> tClass,
                      final Integer[][] patterns,
                      final double[] weightByIndex,
                      final List<Integer> ordering) {
        this.N = n;
        this.nominalGround = nominalGround;
        this.width = width;
        this.height = height;
        this.sample = sample;
        this.distinctValues = distinctValues;
        this.tClass = tClass;
        this.patterns = patterns;
        this.weightByIndex = weightByIndex;
        this.ordering = ordering;
    }


    public Integer[][] getPatterns() {
        return patterns;
    }

    public int getT() {
        return weightByIndex.length;
    }

    public int computeGround() {
        return (nominalGround + getT()) % getT();
    }

    public int getN() {
        return N;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Integer[][] getSample() {
        return sample;
    }

    public int getDistinctValuesCount() {
        return distinctValues.size();
    }

    public List<T> getDistinctValues() {
        return distinctValues;
    }

    public Class<T> gettClass() {
        return tClass;
    }

    public List<Integer> getOrdering() {
        return ordering;
    }

    public double getWeight(final int index) {
        return weightByIndex[index];
    }

}
