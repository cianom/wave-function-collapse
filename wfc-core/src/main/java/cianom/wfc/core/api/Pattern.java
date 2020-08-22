package cianom.wfc.core.api;

import cianom.lib.Boundary;

import java.util.*;
import java.util.function.BiFunction;

public class Pattern {

    private final Integer[] data;
    private final int w;
    private final int h;
    private final double frequency;

    private Integer index;
    private Map<Boundary, List<Pattern>> compatibilies;

    public Pattern(final Integer[] data, final int w, final int h, final double frequency) {
        this.data = data;
        this.w = w;
        this.h = h;
        this.frequency = frequency;
        this.compatibilies = new HashMap<>();
    }

    public int getN() {
        return w;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public List<Pattern> getCompatabilities(final Boundary b) {
        return this.compatibilies.get(b);
    }

    public double getFrequency() {
        return frequency;
    }

    public double getFrequencyLogFrequency() {
        return frequency  * Math.log(frequency);
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

    void refreshCompatabilities(final Map<Boundary, List<Pattern>> newCompatibilies) {
        this.compatibilies = newCompatibilies;
    }

    public int computeId(final int uniqueValuesCount) {
        int result = 0, power = 1;
        for (int i = 0; i < length(); i++) {
            result += value(length() - 1 - i) * power;
            power *= uniqueValuesCount;
        }
        return result;
    }

    private Pattern map(final BiFunction<Integer, Integer, Integer> f) {
        Integer[] result = new Integer[data.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                result[index(x, y)] = f.apply(x, y);
            }
        }

        return new Pattern(result, w, h, frequency);
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

    public Pattern incrFreq(final double amount) {
        return new Pattern(data, w, h, frequency + amount);
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "data=" + Arrays.toString(data) +
                ", w=" + w +
                ", h=" + h +
                ", frequency=" + frequency +
                '}';
    }

    public boolean agrees(final Pattern other, final Boundary b, final int N) {
        final int xmin = Math.max(b.x, 0);
        final int xmax = b.x < 0 ? b.x + N : N;
        final int ymin = Math.max(b.y, 0);
        final int ymax = b.y < 0 ? b.y + N : N;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (!Objects.equals(this.getData()[x + N * y], other.getData()[x - b.x + N * (y - b.y)])) return false;
            }
        }
        return true;
    }


}
