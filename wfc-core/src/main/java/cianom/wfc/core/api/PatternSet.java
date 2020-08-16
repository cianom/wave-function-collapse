package cianom.wfc.core.api;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class PatternSet<T> {


    /**
     * The size of the pattern sample.
     */
    private final int N;
    private final int nominalGround;
    private final List<T> distinctValues;
    private final Class<T> valueClass;


    private final LinkedHashMap<Integer, Pattern> patternsById;
    private final List<Pattern> patternsByIndex;

    public PatternSet(final int n,
                      final int nominalGround,
                      final List<T> distinctValues,
                      final Class<T> valueClass,
                      final LinkedHashMap<Integer, Pattern> patternsById) {
        this.N = n;
        this.nominalGround = nominalGround;
        this.distinctValues = distinctValues;
        this.valueClass = valueClass;
        this.patternsById = patternsById;
        this.patternsByIndex = new ArrayList<>(patternsById.values());
    }

    public final Pattern getPatternByIndex(final int index) {
        return patternsByIndex.get(index);
    }

    public final Pattern getPatternById(final int id) {
        return patternsById.get(id);
    }

    public final List<Pattern> getPatterns() {
        return patternsByIndex;
    }

    public int getPatternCount() {
        return patternsById.size();
    }

    public int computeGround() {
        return (nominalGround + getPatternCount()) % getPatternCount();
    }

    public int getN() {
        return N;
    }

    public int getDistinctValuesCount() {
        return distinctValues.size();
    }

    public List<T> getDistinctValues() {
        return distinctValues;
    }

    public Class<T> getValueClass() {
        return valueClass;
    }
}
