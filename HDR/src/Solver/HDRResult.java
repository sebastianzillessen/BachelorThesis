package Solver;


import Maths.DecimalVector;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
public class HDRResult {

    private final DecimalVector E;
    private final DecimalVector g;
    private final int height;
    private final int width;

    public HDRResult(DecimalVector E, DecimalVector g, int width, int height) {
        if (g.length() != 256)
            throw new IndexOutOfBoundsException("G should be a vector of 256 elements!");
        this.E = E;
        this.g = g;
        this.width = width;
        this.height = height;
    }

    public DecimalVector getG() {
        return g;
    }

    public DecimalVector getE() {
        return E;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double[][] toMap() {
        double r[][] = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                r[i][j] = E.get(i + width * j);
            }
        }
        return r;
    }
}
