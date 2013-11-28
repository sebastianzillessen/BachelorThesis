package Solver;

import Model.HDRResult;
import Model.Image;

import javax.swing.*;
import java.util.List;

/**
 * This class represents a abstract implementation of an IHDRSolver.
 *
 * It can be used to implement other HDR-Solvers if desired.
 *
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 */
public abstract class IHDRSolver extends SwingWorker<HDRResult, HDRResult> {
    List<Image> images;

    protected IHDRSolver(List<Image> images) {
        this.images = images;
    }

    private IHDRSolver() {

    }


    /**
     * Returns the grey value i of picture j
     *
     * @param i index of grey value
     * @param j index of picture
     * @return greyvalue at index i of picture j
     */
    protected int Z(int i, int j) {
        return images.get(j).getValue(i);
    }


    /**
     * returns the time of the exposure time of the picture j
     *
     * @param j index of picture
     * @return exposure time of picture j
     */
    protected double t(int j) {
        return images.get(j).getExposureTime();
    }

    /**
     * the weighning function which should be used for the algorithm. Default is a triangle function.
     *
     * @param z greyvalue
     * @return weight for the greyvalue z
     */
    protected double w(double z) {
        return Math.max((z <= 127) ? z + 1 : 256 - z, 0.0001);
    }
}
