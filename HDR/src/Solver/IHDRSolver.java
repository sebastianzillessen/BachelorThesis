package Solver;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class IHDRSolver extends SwingWorker<HDRResult, HDRResult> implements IWeight {
    List<Image> images;

    protected IHDRSolver(List<Image> images) {
        this.images = images;
    }

    private IHDRSolver() {

    }


    protected int Z(int i, int j) {
        int z = images.get(j).getValue(i);
        if (z < 0 || z > 256)
            System.out.printf("Error: %2d,%2d = %3d", i, j, z);
        return z;
    }

    protected double t(int j) {
        return images.get(j).getExposureTime();
    }

    public double w(double i) {
        return Math.max((i <= 127) ? i + 1 : 256 - i, 0.0001);
    }
}
