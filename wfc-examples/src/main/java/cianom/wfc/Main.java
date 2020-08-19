package cianom.wfc;

import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;
import cianom.wfc.core.api.Pipeline;
import cianom.wfc.core.api.PipelineBuilder;
import cianom.wfc.core.pipe.image.ImageWriterPipe;
import cianom.wfc.core.pipe.pattern.PatternPrinter;
import cianom.wfc.core.pipe.solver.Solver;
import cianom.wfc.core.pipe.pattern.PixelPatternSetReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;

import static cianom.lib.Timing.time;

public class Main {


    static void runOverlappingModel() {
        try {

            final Pipeline<URL, BufferedImage> pipeline = PipelineBuilder
                    .begin(new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 0)))
                    .then(new PatternPrinter<>())
                    .then(new Solver<>(new Solver.ModelConfig(0, 32, 32, 32, false)))
                    .then(new ImageWriterPipe())
                    .build();


            final BufferedImage output = time("run", () ->
                pipeline.run(Main.class.getClassLoader().getResource("image/mond.png"))
            );

            if (output != null) {
                File output_file = new File("example" + "_out.png");
                ImageIO.write(output, "png", output_file);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                    .begin(new Solver<RichTile>(new Solver.ModelConfig(1000000, new Random().nextInt(), w, h, false)))
                    .build();

            final Solver.Solution<RichTile> output = time("run", () ->
//                    pipeline.run(new PatternSet<>(2,
                    pipeline.run(new PatternSet<>(3,
                            0,
                            Arrays.asList(RichTile.OPEN, RichTile.CLOSE, RichTile.TWO, RichTile.THREE),
                            RichTile.class,
                            new LinkedHashMap<Integer, Pattern>() {{
                                put(0, new Pattern(new Integer[] {0, 1, 0, 0, 1, 0, 0, 1, 0}, 3, 3, 1D));
                                put(1, new Pattern(new Integer[] {0, 1, 0, 0, 1, 0, 0, 0, 0}, 3, 3, 1D));
                                put(2, new Pattern(new Integer[] {0, 0, 0, 0, 0, 0, 0, 1, 0}, 3, 3, 1D));
                                put(3, new Pattern(new Integer[] {0, 1, 0, 0, 0, 0, 0, 0, 0}, 3, 3, 1D));
                                put(4, new Pattern(new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0}, 3, 3, 1D));
//
//                                put(0, new Pattern(new Integer[] { 0, 0, 1, 1}, 2, 2, 6.0));
//                                put(1, new Pattern(new Integer[] { 0, 0, 1, 0}, 2, 2, 1.0));
//                                put(2, new Pattern(new Integer[] { 0, 0, 0, 0}, 2, 2, 4.0));
//                                put(3, new Pattern(new Integer[] { 0, 0, 0, 1}, 2, 2, 2.0));
//                                put(4, new Pattern(new Integer[] { 1, 1, 1, 2}, 2, 2, 1.0));
//                                put(5, new Pattern(new Integer[] { 1, 1, 2, 2}, 2, 2, 1.0));
//                                put(6, new Pattern(new Integer[] { 1, 1, 2, 1}, 2, 2, 1.0));
//                                put(7, new Pattern(new Integer[] { 1, 0, 1, 0}, 2, 2, 4.0));
//                                put(8, new Pattern(new Integer[] { 0, 1, 1, 1}, 2, 2, 1.0));
//                                put(9, new Pattern(new Integer[] { 1, 2, 1, 2}, 2, 2, 2.0));
//                                put(10, new Pattern(new Integer[] { 2, 2, 2, 2}, 2, 2, 2.0));
//                                put(11, new Pattern(new Integer[] { 2, 1, 2, 1}, 2, 2, 2.0));
//                                put(12, new Pattern(new Integer[] { 0, 1, 0, 1}, 2, 2, 3.0));
//                                put(13, new Pattern(new Integer[] { 1, 1, 1, 3}, 2, 2, 1.0));
//                                put(14, new Pattern(new Integer[] { 1, 1, 3, 3}, 2, 2, 1.0));
//                                put(15, new Pattern(new Integer[] { 1, 1, 3, 1}, 2, 2, 1.0));
//                                put(16, new Pattern(new Integer[] { 1, 1, 1, 1}, 2, 2, 3.0));
//                                put(17, new Pattern(new Integer[] { 1, 3, 1, 3}, 2, 2, 1.0));
//                                put(18, new Pattern(new Integer[] { 3, 3, 3, 3}, 2, 2, 1.0));
//                                put(19, new Pattern(new Integer[] { 3, 1, 3, 1}, 2, 2, 1.0));
//                                put(20, new Pattern(new Integer[] { 1, 2, 1, 1}, 2, 2, 1.0));
//                                put(21, new Pattern(new Integer[] { 2, 2, 1, 1}, 2, 2, 1.0));
//                                put(22, new Pattern(new Integer[] { 2, 1, 1, 1}, 2, 2, 1.0));
//                                put(23, new Pattern(new Integer[] { 1, 3, 1, 1}, 2, 2, 1.0));
//                                put(24, new Pattern(new Integer[] { 3, 3, 1, 1}, 2, 2, 1.0));
//                                put(25, new Pattern(new Integer[] { 3, 1, 1, 1}, 2, 2, 1.0));
//                                put(26, new Pattern(new Integer[] { 1, 1, 0, 0}, 2, 2, 7.0));
//                                put(27, new Pattern(new Integer[] { 1, 0, 0, 0}, 2, 2, 1.0));
//                                put(28, new Pattern(new Integer[] { 0, 1, 0, 0}, 2, 2, 1.0));
                            }}
                            )
                    )
            );

            if (output != null) {
                for (int j = 0; j < h; j++) {
                    for (int i=0; i < w; i++) {
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
        runOverlappingModel();
    }
}
