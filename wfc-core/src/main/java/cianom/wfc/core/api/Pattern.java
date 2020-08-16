package cianom.wfc.core.api;

import java.util.function.BiFunction;

public class Pattern {

    private final Integer[] data;
    private final int w;
    private final int h;

    public Pattern(final Integer[] data, final int w, final int h) {
        this.data = data;
        this.w = w;
        this.h = h;
    }

    public Integer[] getData() {
        return data;
    }

    public Integer value(final int index) {
        return data[index];
    }

    public int length() {
        return data.length;
    }

    private Pattern map(final BiFunction<Integer, Integer, Integer> f) {
        Integer[] result = new Integer[data.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                result[index(x, y)] = f.apply(x, y);
            }
        }

        return new Pattern(result, w, h);
    }

    private int index(final int x, final int y) {
        return x + y * w;
    }

    public Pattern rotate() {
        return map((Integer x, Integer y) -> data[(h - 1 - y) + (x * w)]
        );
    }

    public Pattern reflect() {
        return map((Integer x, Integer y) -> data[w - 1 - x + y * h]
        );
    }

/*
    array pattern
    N*N
    Adjacency rules
    Frequency hints
     */

}
