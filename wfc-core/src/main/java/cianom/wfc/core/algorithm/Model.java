package cianom.wfc.core.algorithm;


import cianom.lib.ArrayUtil;
import cianom.lib.Boundary;
import cianom.lib.IntPoint;
import cianom.wfc.core.in.PatternSet;
import cianom.wfc.core.out.Target;

import java.util.*;


public abstract class Model<T> {

  protected boolean[][] wave;
  protected final Map<Boundary, int[][]> propagator;
  int[][][] compatible;
  protected int[] observed;

  IntPoint[] stack;
  int stacksize;

  protected final PatternSet<T> in;
  private final Target<T> out;

  protected boolean periodic;
  double[] weightLogWeights;

  int[] sumsOfOnes;
  double sumOfWeights;
  double sumOfWeightLogWeights;
  double startingEntropy;
  double[] sumsOfWeights;
  double[] sumsOfWeightLogWeights;
  double[] entropies;

  protected Model(final PatternSet<T> in,
                  final Target<T> out) {
    this.in = in;
    this.out = out;
    this.propagator = new HashMap<>();
  }

  protected boolean onBoundary(int x, int y) {
    return (
            !this.periodic &&
                    (x + in.getN() > out.getWidth() || y + in.getN() > out.getHeight() || x < 0 || y < 0)
    );

  }


  void init() {

    // Build propagator
    final int T = in.getT();
    propagator.clear();
    final Integer[][] patterns = in.getPatterns();
    for (final Boundary b : Boundary.values()) {
      final int[][] xx = new int[T][];
      this.propagator.put(b, xx);
      for (int t = 0; t < T; t++) {
        final List<Integer> list = new ArrayList<>();
        for (int t2 = 0; t2 < T; t2++) {
          if (agrees(patterns[t], patterns[t2], b, in.getN())) {
            list.add(t2);
          }
        }
        xx[t] = new int[list.size()];
        for (int c = 0; c < list.size(); c++) {
          xx[t][c] = list.get(c);
        }
      }
    }
    // End build propagator

    this.wave = new boolean[out.getWidth() * out.getHeight()][];
    this.compatible = new int[this.wave.length][][];
    for (int i = 0; i < wave.length; i++) {
      this.wave[i] = new boolean[T];
      this.compatible[i] = new int[T][];
      for (int t = 0; t < T; t++) this.compatible[i][t] = new int[4];
    }

    this.weightLogWeights = new double[T];
    this.sumOfWeights = 0;
    this.sumOfWeightLogWeights = 0;

    for (int t = 0; t < T; t++) {
      final double weight = in.getWeight(t);
      this.weightLogWeights[t] = weight * Math.log(weight);
      this.sumOfWeights += weight;
      this.sumOfWeightLogWeights += this.weightLogWeights[t];
    }

    this.startingEntropy =
      Math.log(this.sumOfWeights) - (this.sumOfWeightLogWeights / this.sumOfWeights);

    this.sumsOfOnes = new int[out.getWidth() * out.getHeight()];
    this.sumsOfWeights = new double[out.getWidth() * out.getHeight()];
    this.sumsOfWeightLogWeights = new double[out.getWidth() * out.getHeight()];
    this.entropies = new double[out.getWidth() * out.getHeight()];

    this.stack = new IntPoint[this.wave.length * T];
    this.stacksize = 0;
  }

  Boolean observe(final Random random) {
    final int T = in.getT();
    double min = 1e+3;
    int argmin = -1;

    for (int i = 0; i < this.wave.length; i++) {
      if (this.onBoundary(i % out.getWidth(), i / out.getWidth())) continue;

      int amount = this.sumsOfOnes[i];
      if (amount == 0) return false;

      double entropy = this.entropies[i];

      if (amount > 1 && entropy <= min) {
        double noise = 1e-6 * random.nextDouble();
        if (entropy + noise < min) {
          min = entropy + noise;
          argmin = i;
        }
      }
    }

    // If all cells are at entropy 0, processing is complete:
    if (argmin == -1) {
      // Build collapsed observations for completion
      this.observed = new int[out.getWidth() * out.getHeight()];
      for (int i = 0; i < this.wave.length; i++) {
        for (int t = 0; t < T; t++) {
          if (this.wave[i][t]) {
            final int x = i % out.getWidth();
            final int y = i / out.getWidth();
            int dx = x < out.getWidth() - in.getN() + 1 ? 0 : in.getN() - 1;
            int dy = y < out.getHeight() - in.getN() + 1 ? 0 : in.getN() - 1;
            int idx = dx + dy * in.getN();
//            int t = this.observed[x - dx + (y - dy) * this.targetWidth];
            final Integer xxxx = in.getPatterns()[t][idx];
            final T v = in.getDistinctValues().get(xxxx);
//            in.getPatterns()[t][dx + dy * conf.N]
//            this.in.getDistinctValues().get();
//            Object c = in.getDistinctValues().get(
//                                    in.getSample()[t][dx +
//                                            dy *
//                                                    conf.N]
//                            );
//            Object c =
//                            this.sample.getDistinctValues().get(
//                                    this.patterns[this.observed[x - dx + (y - dy) * this.targetWidth]][dx +
//                                            dy *
//                                                    conf.N]
//                            );
            out.observe(x, y, v);
            this.observed[i] = t;
            break;
          }
        }
      }
      return true;
    }
    else {
      // Choose a pattern by a random sample, weighted by the pattern frequency in the source data
      double[] distribution = new double[T];
      for (int t = 0; t < T; t++) {
        distribution[t] = this.wave[argmin][t] ? in.getWeight(t) : 0;
      }

      final int r = ArrayUtil.weightedRandomIndex(distribution, random.nextDouble());

      boolean[] w = this.wave[argmin];
      for (int t = 0; t < T; t++) {
        if (w[t] != (t == r)) {
          // Set the boolean array in this cell to false, except for the chosen pattern
          this.ban(argmin, t);
        }
      }

      return null;
    }
  }

  protected void ban(int i, int t) {
    this.wave[i][t] = false;

    int[] comp = this.compatible[i][t];
    for (int d = 0; d < 4; d++) comp[d] = 0;
    this.stack[this.stacksize] = new IntPoint(i, t);
    this.stacksize++;

    this.sumsOfOnes[i] -= 1;
    this.sumsOfWeights[i] -= in.getWeight(t);
    this.sumsOfWeightLogWeights[i] -= this.weightLogWeights[t];

    double sum = this.sumsOfWeights[i];
    this.entropies[i] = Math.log(sum) - this.sumsOfWeightLogWeights[i] / sum;
  }

  protected void propagate() {
    while (this.stacksize > 0) {
      IntPoint e1 = this.stack[this.stacksize - 1];
      this.stacksize--;

      int i1 = e1.getFirst();
      int x1 = i1 % out.getWidth();
      int y1 = i1 / out.getWidth();

      for (int d = 0; d < 4; d++) {
        final Boundary b = Boundary.values()[d];
        int x2 = x1 + b.x, y2 = y1 + b.y;

        if (this.onBoundary(x2, y2)) continue;

        if (x2 < 0) x2 += out.getWidth(); else if (x2 >= out.getWidth()) x2 -= out.getWidth();
        if (y2 < 0) y2 += out.getHeight(); else if (y2 >= out.getHeight()) y2 -= out.getHeight();

        int i2 = x2 + y2 * out.getWidth();
        int[] p = this.propagator.get(b)[e1.getSecond()];
        int[][] compat = this.compatible[i2];

        for (final int t2 : p) {
          int[] comp = compat[t2];

          comp[d]--;

          if (comp[d] == 0) this.ban(i2, t2);
        }
      }
    }
  }

  public boolean run(final int seed, final int limit) {
    if (this.wave == null) this.init();

    this.clear();
    final Random random = new Random(seed);

    for (int l = 0; l < limit || limit == 0; l++) {
      Boolean result = this.observe(random);
      if (result != null) return result;
      this.propagate();
    }

    return true;
  }

  protected void clear() {
    final int T = in.getT();
    final int ground = in.computeGround();

    for (int i = 0; i < this.wave.length; i++) {
      for (int t = 0; t < T; t++) {
        this.wave[i][t] = true;
        for (int d = 0; d < 4; d++) {
          final Boundary b = Boundary.values()[d];
          this.compatible[i][t][d] =
                  this.propagator.get(b.opposite())[t].length;
        }
      }

      this.sumsOfOnes[i] = in.getT();
      this.sumsOfWeights[i] = this.sumOfWeights;
      this.sumsOfWeightLogWeights[i] = this.sumOfWeightLogWeights;
      this.entropies[i] = this.startingEntropy;

      // Overlapping clear
      if (ground != 0) {
        for (int x = 0; x < out.getWidth(); x++) {
          for (int t = 0; t < T; t++) {
            if (t != ground) {
              this.ban(x + (out.getHeight() - 1) * out.getWidth(), t);
            }
          }

          for (int y = 0; y < out.getHeight() - 1; y++) {
            this.ban(x + y * out.getWidth(), ground);
          }
        }

        this.propagate();
      }
    }
  }

  Boolean agrees(final Integer[] patterns1, final Integer[] patterns2, final Boundary b, final int N) {
    final int xmin = Math.max(b.x, 0);
    final int xmax = b.x < 0 ? b.x + N : N;
    final int ymin = Math.max(b.y, 0);
    final int ymax = b.y < 0 ? b.y + N : N;

    for (int y = ymin; y < ymax; y++) {
      for (int x = xmin; x < xmax; x++) {
        if (!Objects.equals(patterns1[x + N * y], patterns2[x - b.x + N * (y - b.y)])) return false;
      }
    }
    return true;
  }

}
