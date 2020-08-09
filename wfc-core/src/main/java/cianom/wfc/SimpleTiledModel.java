//package cianom.wfc;
//
//
//import cianom.lib.Boundary;
//
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.util.List;
//import java.util.*;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//import java.util.stream.IntStream;
//
///**
// *
// */
//public class SimpleTiledModel extends Model {
//
//    List<Color[]> tiles;
//    List<String> tilenames;
//    private final boolean black;
//    private final int tilesize;
//
//    /**
//     * Create a new instance of a Simple Tiled Model.
//     *
//     * @param tilesize       Size of the tile images in pixels.
//     * @param tileSymmetries Array of Map of tilenames and their symmetries.
//     * @param neighborData   Array of Map of left and right neighbor combinations.
//     * @param subsetData     Map of Subset definitions.
//     * @param tileData       Map of tile image data indexed by tilename.
//     * @param subsetName     Name of the subset in subsetData to use.
//     * @param width          Output width in tiles.
//     * @param height         Output height in tiles.
//     * @param periodic       Should the output generation be tileable.
//     * @param black
//     * @param unique
//     */
//    public SimpleTiledModel(
//            int tilesize,
//            List<Map<String, String>> tileSymmetries,
//            List<Map<String, String>> neighborData,
//            Map<String, String[]> subsetData,
//            Map<String, BufferedImage> tileData,
//            String subsetName,
//            int width,
//            int height,
//            boolean periodic,
//            boolean black,
//            boolean unique
//    ) {
//        super(width, height);
//        this.periodic = periodic;
//        this.black = black;
//        this.tilesize = tilesize;
//
//        List<String> subset = null;
//        if (
//                subsetName != null &&
//                        subsetData != null &&
//                        subsetData.containsKey(subsetName)
//        ) {
//            subset = Arrays.asList(subsetData.get(subsetName));
//        }
//
//
//        Function<BiFunction<Integer, Integer, Color>, Color[]> tile =
//                (BiFunction<Integer, Integer, Color> f) -> {
//                    Color[] result = new Color[this.tilesize * this.tilesize];
//                    for (int y = 0; y < this.tilesize; y++)
//                        for (int x = 0; x <
//                                this.tilesize; x++)
//                            result[x + y * tilesize] = f.apply(x, y);
//                    return result;
//                };
//
//        Function<Color[], Color[]> rotate =
//                (Color[] array) -> tile.apply(
//                        (Integer x, Integer y) -> array[this.tilesize -
//                                1 -
//                                y +
//                                x *
//                                        this.tilesize]
//                );
//
//        this.tiles = new ArrayList<>();
//        this.tilenames = new ArrayList<>();
//
//        List<Double> tempStationary = new ArrayList<Double>();
//        List<Integer[]> action = new ArrayList<>();
//        HashMap<String, Integer> firstOccurrence = new HashMap<String, Integer>();
//
//        for (Map<String, String> xtile : tileSymmetries) {
//            String tilename = xtile.get("name");
//
//            Function<Integer, Integer> a, b;
//            int cardinality;
//
//            String sym = xtile.getOrDefault("symmetry", "X");
//
//            if (subset != null && !subset.contains(tilename)) continue;
//
//            switch (sym) {
//                // Corner
//                // Diagonal
//                // T
//                // Cross
//                // Line
//                // Empty
//                case "L":
//                    cardinality = 4;
//                    a = (Integer i) -> (i + 1) % 4;
//                    b = (Integer i) -> (i % 2) == 0 ? i + 1 : i - 1;
//                    break;
//                case "T":
//                    cardinality = 4;
//                    a = (Integer i) -> (i + 1) % 4;
//                    b = (Integer i) -> (i % 2) == 0 ? i : 4 - i;
//                    break;
//                case "I":
//                    cardinality = 2;
//                    a = (Integer i) -> 1 - i;
//                    b = (Integer i) -> i;
//                    break;
//                case "\\":
//                    cardinality = 2;
//                    a = (Integer i) -> 1 - i;
//                    b = (Integer i) -> 1 - i;
//                    break;
//                default:
//                    cardinality = 1;
//                    a = (Integer i) -> i;
//                    b = (Integer i) -> i;
//                    break;
//            }
//
//            this.T = action.size();
//            firstOccurrence.put(tilename, this.T);
//
//            Integer[][] map = new Integer[cardinality][];
//            for (int t = 0; t < cardinality; t++) {
//                map[t] = new Integer[8];
//
//                map[t][0] = t;
//                map[t][1] = a.apply(t);
//                map[t][2] = a.apply(a.apply(t));
//                map[t][3] = a.apply(a.apply(a.apply(t)));
//                map[t][4] = b.apply(t);
//                map[t][5] = b.apply(a.apply(t));
//                map[t][6] = b.apply(a.apply(a.apply(t)));
//                map[t][7] = b.apply(a.apply(a.apply(a.apply(t))));
//
//                for (int s = 0; s < 8; s++) map[t][s] += this.T;
//
//                action.add(map[t]);
//            }
//
//            if (unique) {
//                for (int t = 0; t < cardinality; t++) {
//                    BufferedImage xtileData = tileData.get(tilename);
//                    this.tiles.add(
//                            tile.apply(
//                                    (Integer x, Integer y) -> new Color(xtileData.getRGB(x, y))
//                            )
//                    );
//                    this.tilenames.add(String.format("%s %s", tilename, t));
//                }
//            } else {
//                BufferedImage xtileData = tileData.get(tilename);
//                this.tiles.add(
//                        tile.apply(
//                                (Integer x, Integer y) -> new Color(xtileData.getRGB(x, y))
//                        )
//                );
//
//                this.tilenames.add(String.format("%s 0", tilename));
//
//                for (int t = 1; t < cardinality; t++) {
//                    this.tiles.add(rotate.apply(this.tiles.get(this.T + t - 1)));
//                    this.tilenames.add(String.format("%s %s", tilename, t));
//                }
//            }
//
//            for (int t = 0; t < cardinality; t++)
//                tempStationary.add(
//                        Double.valueOf(xtile.getOrDefault("weight", "1.0"))
//                );
//        }
//
//        this.T = action.size();
//        this.weights = tempStationary.toArray(new Double[0]);
//
//        boolean[][][] tempPropagator = new boolean[4][][];
//        for (int d = 0; d < 4; d++) {
//            final Boundary b = Boundary.values()[d];
//            tempPropagator[d] = new boolean[this.T][];
//            this.propagator.put(b, new int[this.T][]);
//            for (int t = 0; t < this.T; t++)
//                tempPropagator[d][t] =
//                        new boolean[this.T];
//        }
//
//
//        for (Map<String, String> xneighbor : neighborData) {
//
//            String[] left = Arrays
//                    .stream(xneighbor.get("left").split(" "))
//                    .filter(x -> !x.isEmpty())
//                    .toArray(String[]::new);
//
//            String[] right = Arrays
//                    .stream(xneighbor.get("right").split(" "))
//                    .filter(x -> !x.isEmpty())
//                    .toArray(String[]::new);
//
//            if (
//                    subset != null &&
//                            (!subset.contains(left[0]) || !subset.contains(right[0]))
//            ) continue;
//
//
//            int L = action.get(firstOccurrence.get(left[0]))[left.length == 1 ? 0
//                    : Integer.parseInt(left[1])];
//            int D = action.get(L)[1];
//
//            int R = action.get(firstOccurrence.get(right[0]))[right.length == 1 ? 0
//                    : Integer.parseInt(right[1])];
//            int U = action.get(R)[1];
//
//
//            tempPropagator[0][R][L] = true;
//            tempPropagator[0][action.get(R)[6]][action.get(L)[6]] = true;
//            tempPropagator[0][action.get(L)[4]][action.get(R)[4]] = true;
//            tempPropagator[0][action.get(L)[2]][action.get(R)[2]] = true;
//
//            tempPropagator[1][U][D] = true;
//            tempPropagator[1][action.get(D)[6]][action.get(U)[6]] = true;
//            tempPropagator[1][action.get(U)[4]][action.get(D)[4]] = true;
//            tempPropagator[1][action.get(D)[2]][action.get(U)[2]] = true;
//        }
//
//        for (int t2 = 0; t2 < this.T; t2++)
//            for (int t1 = 0; t1 < this.T; t1++) {
//                tempPropagator[2][t2][t1] = tempPropagator[0][t1][t2];
//                tempPropagator[3][t2][t1] = tempPropagator[1][t1][t2];
//            }
//
//        List<List<List<Integer>>> sparsePropagator = new ArrayList<>();
//
//        for (int d = 0; d < 4; d++) {
//            sparsePropagator.add(d, new ArrayList<>());
//            for (int t = 0; t < this.T; t++)
//                sparsePropagator.get(d).add(t, new ArrayList<>());
//        }
//
//        for (int d = 0; d < 4; d++) {
//            final Boundary b = Boundary.values()[d];
//            for (int t1 = 0; t1 < this.T; t1++) {
//                List<Integer> sp = sparsePropagator.get(d).get(t1);
//                boolean[] tp = tempPropagator[d][t1];
//
//                for (int t2 = 0; t2 < this.T; t2++) {
//                    if (tp[t2]) sp.add(t2);
//                }
//
//                int ST = sp.size();
//                final int[] sts = new int[ST];
//                this.propagator.get(b)[t1] = sts;
//                for (int st = 0; st < ST; st++) {
//                    sts[st] = sp.get(st);
//                }
//
//            }
//        }
//    }
//
//    @Override
//    protected boolean onBoundary(int x, int y) {
//        return !this.periodic && (x < 0 || y < 0 || x >= this.targetWidth || y >= this.targetHeight);
//    }
//
//    public String textOutput() {
//        StringBuilder result = new StringBuilder();
//
//        for (int y = 0; y < this.targetHeight; y++) {
//            for (int x = 0; x < this.targetWidth; x++)
//                result.append(String.format("{%s}, ", this.tilenames.get(this.observed[x + y * this.targetWidth])));
//            result.append("\n");
//        }
//
//        return result.toString();
//    }
//
//    @Override
//    public BufferedImage graphics() {
//        BufferedImage result = new BufferedImage(
//                this.targetWidth * this.tilesize,
//                this.targetHeight * this.tilesize,
//                BufferedImage.TYPE_INT_RGB
//        );
//
//        if (this.observed != null) {
//            for (int x = 0; x < this.targetWidth; x++)
//                for (int y = 0; y < this.targetHeight; y++) {
//                    Color[] tile = this.tiles.get(this.observed[x + y * this.targetWidth]);
//                    for (int yt = 0; yt < this.tilesize; yt++)
//                        for (int xt = 0; xt <
//                                this.tilesize; xt++) {
//                            Color c = tile[xt + yt * this.tilesize];
//                            result.setRGB(
//                                    x * this.tilesize + xt,
//                                    y * this.tilesize + yt,
//                                    c.getRGB()
//                            );
//                        }
//                }
//        } else {
//            for (int x = 0; x < this.targetWidth; x++)
//                for (int y = 0; y < this.targetHeight; y++) {
//                    boolean[] a = this.wave[x + y * this.targetWidth];
//                    int amount = IntStream
//                            .range(0, a.length)
//                            .map(idx -> a[idx] ? 1 : 0)
//                            .sum();
//
//
//                    final double lambda = 1.0 /
//                            IntStream
//                                    .range(0, this.T)
//                                    .filter(idx -> a[idx])
//                                    .mapToDouble(idx -> this.weights[idx])
//                                    .sum();
//
//                    for (int yt = 0; yt < this.tilesize; yt++)
//                        for (int xt = 0; xt <
//                                this.tilesize; xt++) {
//                            if (this.black && amount == this.T) result.setRGB(
//                                    x * this.tilesize + xt,
//                                    y * this.tilesize * yt,
//                                    (int) 0xff000000
//                            );
//                            else {
//                                double r = 0, g = 0, b = 0;
//                                for (int t = 0; t < this.T; t++)
//                                    if (a[t]) {
//                                        Color c = this.tiles.get(t)[xt + yt * this.tilesize];
//                                        r += c.getRed() * this.weights[t] * lambda;
//                                        g += c.getGreen() * this.weights[t] * lambda;
//                                        b += c.getBlue() * this.weights[t] * lambda;
//                                    }
//
//                                Color newColor = new Color((int) r, (int) g, (int) b);
//                                result.setRGB(
//                                        x * tilesize + xt,
//                                        y * tilesize + yt,
//                                        newColor.getRGB()
//                                );
//                            }
//                        }
//                }
//        }
//
//        return result;
//    }
//}
