package cianom.wfc.core.pipe.solver;

import cianom.lib.Boundary;
import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;

import java.util.*;

public class Propagator {

    private final Map<Boundary, int[][]> propMap;

    public Propagator(final Map<Boundary, int[][]> propMap) {
        this.propMap = propMap;
    }

    public int[] get(final Boundary b, final int pattern) {
        return propMap.get(b)[pattern];
    }

    public static Propagator build(final PatternSet<?> in) {
        final int patternCount = in.getPatternCount();
        final Map<Boundary, int[][]> propa = new HashMap<>();
        for (final Boundary b : Boundary.values()) {
            final int[][] xx = new int[patternCount][];
            propa.put(b, xx);
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
        return new Propagator(propa);
    }

    private static Boolean agrees(final Pattern p1, final Pattern p2, final Boundary b, final int N) {
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
}
