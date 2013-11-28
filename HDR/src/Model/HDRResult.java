package Model;


import Maths.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 10:17
 * To change this template use File | Settings | File Templates.
 */
public class HDRResult {

    private final Vector E;
    private final Vector g;
    private final int height;
    private final int width;

    public HDRResult(Vector E, Vector g, int width, int height) {
        if (g.length() != 256)
            throw new IndexOutOfBoundsException("G should be a vector of 256 elements!");
        this.E = E;
        this.g = g;
        this.width = width;
        this.height = height;
    }

    public Vector getG() {
        return g;
    }

    public Vector getE() {
        return E;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
