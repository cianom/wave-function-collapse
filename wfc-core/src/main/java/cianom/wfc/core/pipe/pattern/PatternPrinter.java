package cianom.wfc.core.pipe.pattern;

import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;
import cianom.wfc.core.api.Pipe;

public class PatternPrinter<T> implements Pipe<PatternSet<T>, PatternSet<T>> {

    @Override
    public PatternSet<T> run(PatternSet<T> ps) throws Exception {

        int count = 0;
        for (final Pattern p : ps.getPatterns()) {
            System.out.println("Pattern " + count++ + ": " + p.toString());
        }

        return ps;
    }
}
