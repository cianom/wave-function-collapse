package cianom.wfc.core.pipe.image;

import cianom.wfc.core.api.Pipe;
import cianom.wfc.core.pipe.solver.Solver;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageWriterPipe implements Pipe<Solver.Solution<Color>, BufferedImage> {


    @Override
    public BufferedImage run(final Solver.Solution<Color> in) {
        final Color[] observed = in.observed;
        final int width = in.width;
        final int height = in.height;

        final BufferedImage result = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        if (observed != null) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final Color c = observed[x + y * width];
                    result.setRGB(x, y, c.getRGB());
                }
            }
        } else {
            throw new IllegalStateException("Not observed");
        }

        return result;
    }

}
