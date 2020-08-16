package cianom.wfc.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipelineBuilder<IN, OUT> {

    private final List<Pipe<?, ?>> pipes;

    private PipelineBuilder(final Pipe<IN, OUT> start) {
        this.pipes = new ArrayList<>(Collections.singletonList(start));
    }

    private PipelineBuilder(final List<Pipe<?, ?>> pipes) {
        this.pipes = pipes;
    }

    public <NEWOUT> PipelineBuilder<IN, NEWOUT> then(final Pipe<OUT, NEWOUT> next) {
        final List<Pipe<?, ?>> copiedPipes = new ArrayList<>(pipes);
        copiedPipes.add(next);
        return new PipelineBuilder<>(copiedPipes);
    }

    public Pipeline<IN, OUT> build() {
        return new Pipeline<IN, OUT>(pipes);
    }

    public static <IN, OUT> PipelineBuilder<IN, OUT> begin(final Pipe<IN, OUT> start) {
        return new PipelineBuilder<>(start);
    }
}
