package cianom.wfc.core.pipe.solver;

import cianom.wfc.core.api.Pattern;

import java.util.Objects;

public class Collapsed<T> {
    public final int index;
    public final int valueInPatternIndex;
    public final Pattern pattern;
    public final T value;

    Collapsed(int index, int valueInPatternIndex, Pattern pattern, T value) {
        this.index = index;
        this.valueInPatternIndex = valueInPatternIndex;
        this.pattern = pattern;
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collapsed<?> collapsed = (Collapsed<?>) o;
        return index == collapsed.index &&
                valueInPatternIndex == collapsed.valueInPatternIndex &&
                pattern.equals(collapsed.pattern) &&
                value.equals(collapsed.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, valueInPatternIndex, pattern, value);
    }

}
