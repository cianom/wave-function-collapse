package cianom.wfc;

import cianom.wfc.core.api.Pipeline;
import cianom.wfc.core.api.PipelineBuilder;
import cianom.wfc.core.pipe.image.ImageWriterPipe;
import cianom.wfc.core.pipe.solver.Solver;
import cianom.wfc.core.pipe.pattern.PixelPatternSetReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import static cianom.lib.Timing.time;

public class Main {


    static void runOverlappingModel() {
        try {

            final Pipeline<URL, BufferedImage> pipeline = PipelineBuilder
                    .begin(new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 2)))
                    .then(new Solver<>(new Solver.ModelConfig(0, 42, 32, 32)))
                    .then(new ImageWriterPipe())
                    .build();

//            final PatternSet<Color> sample = new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 2), imageURL).read();
//
//            final Target<Color> imageOut = new BufferedImageTarget(32, 32);
//            final Solver<Color> solver = new Solver<>(new Solver.ModelConfig(3, 42, 32, 32));
//            final boolean finished = time("run", () ->
//                    solver.run(42, 0)
//            );

            final BufferedImage output = time("run", () ->
                pipeline.run(Main.class.getClassLoader().getResource("image/mond.png"))
            );
//            System.out.println("Finished: " + finished);

            if (output != null) {
//                BufferedImage output = imageOut.toImage();

                File output_file = new File("example" + "_out.png");
                ImageIO.write(output, "png", output_file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//    runTiledModel();
        runOverlappingModel();
    }
}
