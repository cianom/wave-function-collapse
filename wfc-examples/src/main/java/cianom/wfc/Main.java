package cianom.wfc;

import cianom.wfc.core.api.*;
import cianom.wfc.core.pipe.image.ImageWriterPipe;
import cianom.wfc.core.pipe.pattern.PixelPatternSetReader;
import cianom.wfc.core.pipe.solver.Solver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import static cianom.lib.Timing.time;

public class Main {


    static void runOverlappingModel(final int seed, final int outW, final int outH, final boolean periodicOut, final boolean periodicIn) throws Exception {

        System.out.println("Using seed " + seed);
        final Pipeline<URL, BufferedImage> pipeline = PipelineBuilder
                .begin(new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 3, periodicIn, 1)))
                .then(new Solver<>(new Solver.ModelConfig(100_000_000, seed, outW, outH, periodicOut)))
                .then(new ImageWriterPipe())
                .build();

        final BufferedImage output = time("run", () ->
                pipeline.run(Main.class.getClassLoader().getResource("image/flower.png"))
        );

        if (output != null) {
            File output_file = new File(".example" + "_out.png");
            ImageIO.write(output, "png", output_file);
        }
    }


    enum RichTile {
        OPEN,
        CLOSE,
        TWO,
        THREE;

        public char toChar() {
            return name().charAt(0);
        }
    }

    static void runRichModel() {
        try {

            final int w = 32, h = 32;
            final Pipeline<PatternSet<RichTile>, Solver.Solution<RichTile>> pipeline = PipelineBuilder
                    .begin(new Solver<RichTile>(new Solver.ModelConfig(10000, new Random().nextInt(), w, h, false)))
                    .build();

            final Solver.Solution<RichTile> output = time("run", () -> {
//                    pipeline.run(new PatternSet<>(2,
                final PatternSet<RichTile> patternSet = new PatternSetBuilder<RichTile>(3,
                        0,
                        Arrays.asList(RichTile.OPEN, RichTile.CLOSE, RichTile.TWO, RichTile.THREE),
                        RichTile.class)
                        .addPattern(new Pattern(new Integer[]{0, 1, 0, 0, 1, 0, 0, 1, 0}, 3, 3, 1D))
                        .addPattern(new Pattern(new Integer[]{0, 1, 0, 0, 1, 0, 0, 0, 0}, 3, 3, 1D))
                        .addPattern(new Pattern(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1, 0}, 3, 3, 1D))
                        .addPattern(new Pattern(new Integer[]{0, 1, 0, 0, 0, 0, 0, 0, 0}, 3, 3, 1D))
                        .addPattern(new Pattern(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0}, 3, 3, 1D))
                        .build();
                return pipeline.run(patternSet);
            });


            if (output != null) {
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        RichTile a = output.observed[0].value;
                        System.out.print(output.observed[i + j * w].value.toChar() + " ");
                    }
                    System.out.println();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//    runRichModel();
        for (int i = 0; i < 1; i++) {
            try {
//                runOverlappingModel(new Random().nextInt(), 8, 8, false, false);
                runOverlappingModel(1024856989, 256, 256, false, true);
//                runOverlappingModel(new Random().nextInt(), 256, 256, false, true);
            } catch (Exception e) {
                System.out.println("ERROR on iteration " + i);
            }
        }
        // 1024856989
    }
}
