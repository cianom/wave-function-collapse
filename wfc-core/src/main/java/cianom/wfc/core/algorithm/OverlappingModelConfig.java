package cianom.wfc.core.algorithm;

import java.util.Objects;

public class OverlappingModelConfig {

    public final String name;
    public final int symmetry;
    public final boolean periodicOutput;

    /**
     * @param periodicOutput Whether the generation should be periodic (repeatable).
     * @param symmetry       Allowed symmetries from 1 (no symmetry) to 8 (all mirrored / rotated variations).
     */
    public OverlappingModelConfig(final String name,
                                  final int symmetry,
                                  final boolean periodicOutput) {
        this.name = name;
        this.symmetry = symmetry;
        this.periodicOutput = periodicOutput;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlappingModelConfig that = (OverlappingModelConfig) o;
        return symmetry == that.symmetry &&
                periodicOutput == that.periodicOutput &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symmetry, periodicOutput);
    }

    @Override
    public String toString() {
        return "OverlappingModelConfig{" +
                "name='" + name + '\'' +
                ", symmetry=" + symmetry +
                ", periodicOutput=" + periodicOutput +
                '}';
    }

}
