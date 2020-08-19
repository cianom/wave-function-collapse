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
                    .then(new Solver<>(new Solver.ModelConfig(0, 42, 32, 32, false)))
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
        CLOSE;

        public char toChar() {
            return name().charAt(0);
        }
    }
    static void runRichModel() {
        try {

            final int w = 64, h = 8;
            final Pipeline<PatternSet<RichTile>, Solver.Solution<RichTile>> pipeline = PipelineBuilder
                    .begin(new Solver<RichTile>(new Solver.ModelConfig(100000, new Random().nextInt(), w, h, false)))
                    .build();

            final Solver.Solution<RichTile> output = time("run", () ->
                    pipeline.run(new PatternSet<>(3,
                            0,
                            Arrays.asList(RichTile.OPEN, RichTile.CLOSE),
                            RichTile.class,
                            new LinkedHashMap<Integer, Pattern>() {{
                                put(0, new Pattern(new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0}, 3, 3, 1D));
                                put(1, new Pattern(new Integer[] {1, 1, 1, 1, 1, 1, 1, 1, 1}, 3, 3, 10D));
                            }}
                            )
                    )
            );

            if (output != null) {
                for (int j = 0; j < h; j++) {
                    for (int i=0; i < w; i++) {
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
