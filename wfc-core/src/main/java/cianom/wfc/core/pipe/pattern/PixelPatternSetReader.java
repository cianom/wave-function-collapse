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
import java.util.function.BiFunction;

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

        final Pair<double[], List<Integer>> r = calculateWeights(sample, new HashSet<>(colors), new IntPoint(sourceWidth, sourceHeight));

        final Pattern[] patterns = computerPatterns(r.one.length, conf.getN(), colors.size(), r.two);

        return new PatternSet<>(conf.getN(), conf.getNominalGround(), sourceWidth, sourceHeight, sample, colors, Color.class, patterns, r.one, r.two);
    }

    private Pattern[] computerPatterns(final int T, final int N, final int distinctValuesCount, final List<Integer> ordering) {
        final Pattern[] patterns = new Pattern[T];

        int counter = 0;
        final long W = MathUtil.pow(distinctValuesCount, N * N);

        for (final long index : ordering) {
            patterns[counter] = patternFromIndex(index, W, distinctValuesCount, N);

            counter++;
        }

        return patterns;
    }

    private Pattern patternFromIndex(final Long index, final long W, final int colorCount, final int N) {
        long residue = index;
        long power = W;
        Integer[] result = new Integer[N * N];

        for (int i = 0; i < result.length; i++) {
            power /= colorCount;
            int count = 0;

            while (residue >= power) {
                residue -= power;
                count++;
            }

            result[i] = count;
        }

        return new Pattern(result, N, N);
    }


    private Pair<double[], List<Integer>> calculateWeights(final Integer[][] sample,
                                                           final Set<Color> imageColors,
                                                           final IntPoint imageDimensions) {
        final Map<Integer, Double> weights = new HashMap<>();
        final List<Integer> ordering = new ArrayList<>();

        final int readHeight = (conf.isPeriodicInput() ? imageDimensions.getH() : imageDimensions.getH() - conf.getN() + 1);
        final int readWidth = (conf.isPeriodicInput() ? imageDimensions.getW() : imageDimensions.getW() - conf.getN() + 1);
        for (int y = 0; y < readHeight; y++) {
            for (int x = 0; x < readWidth; x++) {
                final Pattern[] ps = new Pattern[8];

                ps[0] = patternFromSample(sample, imageDimensions, x, y);
                ps[1] = ps[0].reflect();
                ps[2] = ps[0].rotate();
                ps[3] = ps[2].reflect();
                ps[4] = ps[2].rotate();
                ps[5] = ps[4].reflect();
                ps[6] = ps[4].rotate();
                ps[7] = ps[6].reflect();

                for (int k = 0; k < conf.getSymmetry(); k++) {
                    int index = indexOf(imageColors.size(), ps[k]);
                    if (weights.containsKey(index)) {
                        weights.put(index, weights.get(index) + 1);
                    } else {
                        weights.put(index, 1D);
                        ordering.add(index);
                    }
                }
            }
        }
        final double[] weightByIndex = new double[weights.size()];
        for (int i = 0; i < ordering.size(); i++) {
            weightByIndex[i] = weights.get(ordering.get(i));
        }

        return new Pair<>(weightByIndex, ordering);
    }

    private Pattern pattern(final BiFunction<Integer, Integer, Integer> f) {
        Integer[] result = new Integer[conf.getN() * conf.getN()];
        for (int y = 0; y < conf.getN(); y++) {
            for (int x = 0; x < conf.getN(); x++) {
                result[x + y * conf.getN()] = f.apply(x, y);
            }
        }

        return new Pattern(result, conf.getN(), conf.getN());
    }

    private Pattern patternFromSample(final Integer[][] sample,
                                      final IntPoint imageDimensions,
                                      final int x,
                                      final int y) {
        return pattern(
                (Integer dx, Integer dy) -> sample[(x + dx) % imageDimensions.getW()][(y + dy) % imageDimensions.getH()]
        );
    }

    private Integer indexOf(final int colorsCount, final Pattern p) {
        int result = 0, power = 1;
        for (int i = 0; i < p.length(); i++) {
            result += p.value(p.length() - 1 - i) * power;
            power *= colorsCount;
        }
        return result;
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
