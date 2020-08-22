package cianom.wfc.core;

import cianom.wfc.core.api.Pipeline;
import cianom.wfc.core.api.PipelineBuilder;
import cianom.wfc.core.pipe.pattern.PixelPatternSetReader;
import cianom.wfc.core.pipe.solver.Solver;
import org.junit.Test;

import java.awt.*;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PipelineComponentTest {

    @Test
    public void snapshot_pipeline_result() throws Exception {

        final Pipeline<URL, Solver.Solution<Color>> pipeline = PipelineBuilder
                .begin(new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 1)))
                .then(new Solver<>(new Solver.ModelConfig(0, 42, 32, 32, false)))
                .build();

        final Solver.Solution<Color> result = pipeline.run(PipelineComponentTest.class.getClassLoader().getResource("image/mond.png"));

        assertEquals(1888971341, Arrays.hashCode(Arrays.stream(result.observed).map(o -> o.value).toArray()));
        assertEquals(-424064624, Arrays.hashCode(result.observed));
    }
}
