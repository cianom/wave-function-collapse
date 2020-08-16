package cianom.wfc.core.api;

import cianom.lib.Timing;

import java.util.Collections;
import java.util.List;

public class Pipeline<IN, OUT> {


    private final List<Pipe<?, ?>> pipes;

    public Pipeline(final List<Pipe<?, ?>> pipes) {
        this.pipes = Collections.unmodifiableList(pipes);
    }

    @SuppressWarnings("unchecked")
    public OUT run(final IN startingInput) throws Exception {

        Object output = startingInput;
        for (final Pipe p : pipes) {
            final Object input = output;
            output = Timing.time(p.getClass().getSimpleName(), () -> p.run(input));
        }
        return (OUT) output;
    }

}
