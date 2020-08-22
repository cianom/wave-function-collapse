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


    static void runOverlappingModel() throws Exception {

        final Pipeline<URL, BufferedImage> pipeline = PipelineBuilder
                .begin(new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 0)))
//                    .then(new PatternPrinter<>())
                .then(new Solver<>(new Solver.ModelConfig(0, new Random().nextInt(), 48, 48, false)))
                .then(new ImageWriterPipe())
                .build();

        final BufferedImage output = time("run", () ->
                pipeline.run(Main.class.getClassLoader().getResource("image/mond.png"))
        );

        if (output != null) {
            File output_file = new File("example" + "_out.png");
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
                        RichTile a = output.observed[0];
                        System.out.print(output.observed[i + j * w].toChar() + " ");
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
        for (int i = 0; i < 100; i++) {
            try {
                runOverlappingModel();
            } catch (Exception e) {
                System.out.println("ERROR on iteration " + i);
            }
        }
    }
}
