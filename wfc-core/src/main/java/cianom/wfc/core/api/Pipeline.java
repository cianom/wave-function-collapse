package cianom.wfc.core.api;

import java.util.Collections;
import java.util.List;

public class Pipeline<IN, OUT> {


    private final List<Pipe<?, ?>> pipes;

    public Pipeline(final List<Pipe<?, ?>> pipes) {
        this.pipes = Collections.unmodifiableList(pipes);
    }

    @SuppressWarnings("unchecked")
    public OUT run(final IN startingInput) throws Exception {

        Object input = startingInput;
        for (final Pipe p : pipes) {
            input = p.run(input);
        }
        return (OUT) input;
    }

}
