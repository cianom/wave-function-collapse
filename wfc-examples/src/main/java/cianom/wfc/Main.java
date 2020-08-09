package cianom.wfc;

import cianom.wfc.core.algorithm.OverlappingModel;
import cianom.wfc.core.algorithm.OverlappingModelConfig;
import cianom.wfc.core.in.PatternSet;
import cianom.wfc.core.out.BufferedImageTarget;
import cianom.wfc.core.out.Target;
import cianom.wfc.image.PixelPatternSetReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Random;

import static cianom.lib.Timing.time;

public class Main {


    static void runOverlappingModel() {
        try {

            final URL imageURL = Main.class.getClassLoader().getResource("image/mond.png");
            final PatternSet<Color> sample = new PixelPatternSetReader(new PixelPatternSetReader.PixelReadConfig(0, 2, true, 2), imageURL).read();
            final Random random = new Random();

            final OverlappingModelConfig conf = new OverlappingModelConfig(
                    "example",
                    2,
                    false);

            final Target<Color> imageOut = new BufferedImageTarget(32, 32);
            final OverlappingModel<Color> model = new OverlappingModel<>(sample, imageOut, conf);
            final boolean finished = time("run", () ->
                    model.run(random.nextInt(), 0)
            );
            System.out.println("Finished: " + finished);

            if (finished) {
                BufferedImage output = imageOut.toImage();

                File output_file = new File(conf.name + "_out.png");
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
