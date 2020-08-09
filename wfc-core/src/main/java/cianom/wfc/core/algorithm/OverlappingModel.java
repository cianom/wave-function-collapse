package cianom.wfc.core.algorithm;


import cianom.lib.Boundary;
import cianom.lib.MathUtil;
import cianom.wfc.core.out.Target;
import cianom.wfc.core.in.PatternSet;

import java.util.ArrayList;
import java.util.List;

public class OverlappingModel<T> extends Model<T> {


    private final OverlappingModelConfig conf;


    /**
     * Creates a new instance of the Overlapping Model
     *
     * @param in input sample to draw from.
     */
    public OverlappingModel(final PatternSet<T> in,
                            final Target<T> out,
                            final OverlappingModelConfig conf) {
        super(in, out);
        this.conf = conf;

//        long W = MathUtil.pow(in.getDistinctValuesCount(), in.getN() * in.getN());
//
//        final List<Integer> ordering = in.getOrdering();


    }



}
