package cianom.wfc.core.pipe.pattern;

import cianom.lib.IntPoint;
import cianom.lib.MathUtil;
import cianom.lib.Pair;
import cianom.wfc.core.api.Pattern;
import cianom.wfc.core.api.PatternSet;
import cianom.wfc.core.api.Pipe;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.*;

public class PixelPatternSetReader implements Pipe<URL, PatternSet<Color>> {

    private final ReadConf conf;


    public PixelPatternSetReader(final ReadConf conf) {
        this.conf = conf;
    }

    @Override
    public PatternSet<Color> run(final URL imageURL) throws Exception {

        final BufferedImage data = ImageIO.read(imageURL.openStream());
        final List<Color> colors = new ArrayList<>();
        final int sourceWidth = data.getWidth();
        final int sourceHeight = data.getHeight();

        Integer[][] sample = new Integer[sourceWidth][sourceHeight];

        // Parse the input, build a full list of colours and index to them.
        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                final Color color = new Color(data.getRGB(x, y));

                // find index of color, adding the color if not already existing.
                int i = colors.indexOf(color);
                if (i == -1) {
                    colors.add(color);
                    i = colors.size() - 1;
                }
                sample[x][y] = i;
            }
        }

        final LinkedHashMap<Integer, Pattern> patterns = scanPatterns(sample, new HashSet<>(colors), new IntPoint(sourceWidth, sourceHeight));


        return new PatternSet<>(conf.getN(), conf.getNominalGround(), sourceWidth, sourceHeight, sample, colors, Color.class, patterns);
    }

    private LinkedHashMap<Integer, Pattern> scanPatterns(final Integer[][] sample,
                                                       final Set<Color> imageColors,
                                                       final IntPoint imageDimensions) {
        final LinkedHashMap<Integer, Pattern> patterns = new LinkedHashMap<>();

        final int sym = conf.getSymmetry();
        final int readHeight = (conf.isPeriodicInput() ? imageDimensions.getH() : imageDimensions.getH() - conf.getN() + 1);
        final int readWidth = (conf.isPeriodicInput() ? imageDimensions.getW() : imageDimensions.getW() - conf.getN() + 1);
        for (int y = 0; y < readHeight; y++) {
            for (int x = 0; x < readWidth; x++) {
                final Pattern[] ps = new Pattern[1 + sym];

                ps[0] = patternFromSample(sample, imageDimensions, x, y);
                if (sym >= 1) ps[1] = ps[0].reflect();
                if (sym >= 2) ps[2] = ps[0].rotate();
                if (sym >= 3) ps[3] = ps[2].reflect();
                if (sym >= 4) ps[4] = ps[2].rotate();
                if (sym >= 5) ps[5] = ps[4].reflect();
                if (sym >= 6) ps[6] = ps[4].rotate();
                if (sym >= 7) ps[7] = ps[6].reflect();

                for (int k = 0; k < sym + 1; k++) {
                    final int id = ps[k].computeId(imageColors.size());

                    patterns.merge(id, ps[k], (existing, added) -> existing.incrFreq(added.getFrequency()));
                }

            }
        }

        return patterns;
    }

    private Pattern patternFromSample(final Integer[][] sample,
                                      final IntPoint imageDimensions,
                                      final int x,
                                      final int y) {

        final Integer[] result = new Integer[conf.getN() * conf.getN()];
        for (int dy = 0; dy < conf.getN(); dy++) {
            for (int dx = 0; dx < conf.getN(); dx++) {
                result[dx + dy * conf.getN()] = sample[(x + dx) % imageDimensions.getW()][(y + dy) % imageDimensions.getH()];
            }
        }
        return new Pattern(result, conf.getN(), conf.getN(), 1D);
    }


    public static class PixelReadConfig implements ReadConf {

        private final int nominalGround;
        private final int N;
        private final boolean periodicInput;
        private final int symmetry;

        public PixelReadConfig(int nominalGround, int n, boolean periodicInput, int symmetry) {
            this.nominalGround = nominalGround;
            N = n;
            this.periodicInput = periodicInput;
            this.symmetry = symmetry;
        }

        @Override
        public int getNominalGround() {
            return nominalGround;
        }

        @Override
        public int getN() {
            return N;
        }

        @Override
        public boolean isPeriodicInput() {
            return periodicInput;
        }

        @Override
        public int getSymmetry() {
            return symmetry;
        }
    }
}
