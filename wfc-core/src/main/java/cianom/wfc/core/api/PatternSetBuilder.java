package cianom.wfc.core.api;

import cianom.lib.Boundary;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatternSetBuilder<T> {


    private final int N;
    private final int nominalGround;
    private final List<T> distinctValues;
    private final Class<T> valueClass;
    private final LinkedHashSet<Pattern> patterns;

    public PatternSetBuilder(final int n,
                              final int nominalGround,
                              final List<T> distinctValues,
                              final Class<T> valueClass) {
        this.N = n;
        this.nominalGround = nominalGround;
        this.distinctValues = distinctValues;
        this.valueClass = valueClass;
        this.patterns = new LinkedHashSet<>();
    }


    public PatternSetBuilder<T> addPatterns(final Collection<Pattern> patterns) {
        this.patterns.addAll(patterns);
        return this;
    }

    public PatternSetBuilder<T> addPattern(final Pattern pattern) {
        this.patterns.add(pattern);
        return this;
    }

    public PatternSet<T> build() {

        // Set index
        final List<Pattern> indexedPatterns = new ArrayList<>(patterns);
        for (int i = 0; i < indexedPatterns.size(); i++) indexedPatterns.get(i).setIndex(i);

        final int patternCount = indexedPatterns.size();

        for (int t = 0; t < patternCount; t++) {
            final Pattern p1 = indexedPatterns.get(t);
            final Map<Boundary, List<Pattern>> newCompatibilies = new HashMap<>();

            for (final Boundary b : Boundary.values()) {
                final List<Pattern> list = new ArrayList<>();
                for (final Pattern p2 : indexedPatterns) {
                    if (p1.agrees(p2, b, p1.getN())) {
                        list.add(p2);
                    }
                }
                newCompatibilies.put(b, list);
            }

            p1.refreshCompatabilities(newCompatibilies);
        }
        final LinkedHashMap<Integer, Pattern> patternsById = indexedPatterns.stream().collect(Collectors.toMap(
                i -> i.computeId(distinctValues.size()),
                Function.identity(),
                (i1, i2) -> { throw new IllegalStateException("Unexpected same keys"); },
                LinkedHashMap::new
                ));

        return new PatternSet<>(N, nominalGround, distinctValues, valueClass, patternsById);
    }


}
