package Solver;

import Ctrl.Controller;
import Display.Plots.ScatterPlot;
import Maths.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */
public class IterativeEnergySolver extends IHDRSolver {
    private double alpha;
    private int GLOBAL_INNER_ITERATIONS = 5;


    public static enum WEIGHTNING_MODES {
        NONE,
        DEFAULT,
        PARABEL

    }

    ;
    private static final double EPSILON_2 = 0.0001d;
    private final int iterations;
    private final int N;
    private final int P;
    private WEIGHTNING_MODES weightMode;
    private double mu = 50;
    private BandMatrix dt, d, weight;
    private final double lambda;

    private int[] histogram;
    private int energySteps = 2;
    private double[] phi_smooth;
    private double[][] phi_data = null;
    private double ln_t[];

    private boolean robustnessDataG = false, robustnessSmoothnessG = false;


    public IterativeEnergySolver(List<Image> images, double lambda, int iterations, int updateInterval, int mu, boolean robustnessDataG, boolean robustnessSmoothnessG, WEIGHTNING_MODES weightMode, double alpha) {
        this(images, lambda, iterations, mu, robustnessDataG, robustnessSmoothnessG, weightMode, alpha);
        energySteps = iterations / updateInterval;
    }

    public IterativeEnergySolver(List<Image> images, double lambda, int iterations, double mu, boolean robustnessDataG, boolean robustnessSmoothnessG, WEIGHTNING_MODES weightMode, double alpha) {
        super(images);
        this.lambda = lambda;
        this.alpha = alpha;
        this.N = images.get(0).getImageSize();
        this.P = images.size();
        this.iterations = iterations;
        this.mu = mu;
        this.robustnessDataG = robustnessDataG;
        this.robustnessSmoothnessG = robustnessSmoothnessG;
        this.weightMode = weightMode;


        System.out.println(this);

        initLnT(images);
        // initialize robustness function with "1"
        initPhiSmooth();
        initPhiData();
        generateOverallHistogramm();
        initWeightMatrix();
        initDerivateMatrices();

        Controller.getInstance().getDisplay().addPlot(new ScatterPlot(histogram), "Histogram");


    }

    private void initPhiData() {
        if (phi_data == null)
            phi_data = new double[images.get(0).getImageSize()][ln_t.length];
        for (int i = 0; i < phi_data.length; i++) {
            for (int j = 0; j < ln_t.length; j++) {
                phi_data[i][j] = 1;
            }
        }
    }

    private void initDerivateMatrices() {
        int size = 256;
        d = new BandMatrix(size, new int[]{-1, 0});
        d.set(0, 0, 0);
        for (int row = 1; row < size; row++) {
            d.set(row, row - 1, -1);
            d.set(row, row, 1);
        }
        dt = d.transpose();
    }

    private void initWeightMatrix() {
        weight = new BandMatrix(256, new int[]{0});
        for (int i = 0; i < weight.cols(); i++) {
            weight.set(i, i, w(i));
        }
    }

    private void generateOverallHistogramm() {
        // generate histogram over all pictures
        int[] d = images.get(0).getHistogram();
        for (int i = 1; i < P; i++) {
            for (int j = 0; j < d.length; j++) {
                d[j] += images.get(i).getHistogram()[j];
            }
        }
        histogram = d;
    }

    private void initLnT(List<Image> images) {
        ln_t = new double[images.size()];
        for (int j = 0; j < ln_t.length; j++) {
            ln_t[j] = Math.log(t(j));
        }
    }

    private void initPhiSmooth() {
        phi_smooth = new double[256];
        for (int k = 0; k < phi_smooth.length; k++) {
            phi_smooth[k] = 1;
        }
    }

    @Override
    public double w(double z) {
        if (this.weightMode == WEIGHTNING_MODES.NONE)
            return 1;
        else if (this.weightMode == WEIGHTNING_MODES.PARABEL)
            return -(1.0 / 129) * z * z + (127.0 / 64) * z;
        else
            return super.w(z);
    }


    protected double w2(int z) {
        return w(z) * w(z);
    }


    private BandMatrix buildDerivateMatrix() {
        int n = 256;
        BandMatrix d = new BandMatrix(n, new int[]{-2, -1, 0, 1, 2});
        int[] basis = new int[]{1, -4, 6, -4, 1};
        d.set(0, 0, +1 * w2(1) / phi_smooth[1]);
        d.set(0, 1, -2 * w2(1) / phi_smooth[1]);
        d.set(0, 2, +1 * w2(1) / phi_smooth[1]);
        // second row
        d.set(1, 0, -2 * w2(1) / phi_smooth[1]);
        d.set(1, 1, 4 * w2(1) / phi_smooth[1] + w2(2) / phi_smooth[2]);
        d.set(1, 2, -2 * (w2(1) / phi_smooth[1] + w2(2) / phi_smooth[2]));
        d.set(1, 3, w2(2) / phi_smooth[2]);
        // diagonale
        for (int row = 2; row < n - 2; row++) {
            // default matrixG 2nd degree on the base.
            d.set(row, row - 2, w2(row - 1) / phi_smooth[row - 1]);
            d.set(row, row - 1, -2 * (w2(row - 1) / phi_smooth[row - 1] + w2(row) / phi_smooth[row]));
            d.set(row, row, w2(row - 1) / phi_smooth[row - 1] + 4 * w2(row) / phi_smooth[row] + w2(row + 1) / phi_smooth[row + 1]);
            d.set(row, row + 1, -2 * (w2(row) / phi_smooth[row] + w2(row + 1) / phi_smooth[row + 1]));
            d.set(row, row + 2, w2(row + 1) / phi_smooth[row + 1]);
        }
        // second last row
        d.set(n - 2, n - 4, w2(253) / phi_smooth[253]);
        d.set(n - 2, n - 3, -2 * (w2(253) / phi_smooth[253] + w2(254) / phi_smooth[254]));
        d.set(n - 2, n - 2, w2(253) / phi_smooth[253] + 4 * w2(254) / phi_smooth[254]);
        d.set(n - 2, n - 1, -2 * w2(254) / phi_smooth[254]);

        // last row
        d.set(n - 1, n - 3, w2(254) / phi_smooth[254]);
        d.set(n - 1, n - 2, -2 * w2(254) / phi_smooth[254]);
        d.set(n - 1, n - 1, w2(254) / phi_smooth[254]);
        return d.mult(2 * lambda);
    }

    private DecimalVector calculateG(DecimalVector F, DecimalVector g, int iteration) {
        int MAX_ITERATIONS = 1;
        if (robustnessDataG || robustnessSmoothnessG || mu > 0) {
            MAX_ITERATIONS = GLOBAL_INNER_ITERATIONS;
            System.out.println("We have robustness or monotonie. So we will iterate the G Process");
        }
        for (int iterations = 0; iterations < MAX_ITERATIONS; iterations++) {
            System.out.print(".");
            update_phi_smooth(g);
            update_phi_data(g, F);
            BandMatrix m = buildDerivateMatrix();
            DecimalVector b = initializeB(F, g);
            // add on the diagonale the DecimalMatrix with the sums of each grayvalue in the picture.
            // Entry (k,k) says how many time the grayvalue k is present overall pictures and
            // is added to the derivate matrix above
            setupDataTerm(m);

            if (mu > 0) {
                m = monotonieConstraint(g, m);
            }
            g = m.solvePentadiagonale(b);
            if (true || (iteration == 0 && iterations == 0)) {
                g = g.subtract(g.get(127));
            }
        }
        System.out.println();
        return g;
    }

    private void setupDataTerm(BandMatrix m) {
        // DATA TERM
        for (int k = 0; k < m.rows(); k++) {
            double t = 0;
            if (robustnessDataG) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < P; j++) {
                        if (Z(i, j) == k) {
                            t += w2(k) / phi_data[i][j];
                        }
                    }
                }
            } else {
                t = w(k) * histogram[k];
            }
            m.set(k, k, m.get(k, k) + t);
        }
    }


    private BandMatrix monotonieConstraint(DecimalVector g, BandMatrix m) {
        BandMatrix v = new BandMatrix(256, new int[]{0});
        int count = 0;
        for (int i = 1; i < g.length(); i++) {
            double diff = g.get(i - 1) - (g.get(i));
            if (diff > 0) {
                v.set(i, i, diff);
                count++;
            }
        }
        BandMatrix mon = dt.mult(v.transpose().mult(weight).mult(v)).mult(d).mult(mu);
        /*if (count > 0)
            mon.toFileSync("calc/mon_" + System.currentTimeMillis() + ".txt");
        else
            System.out.println("No monotonie needed");*/
        return m.add(mon);
    }

    private DecimalVector initializeB(DecimalVector F, DecimalVector oldG) {
        DecimalVector b;
        b = new DecimalVector(oldG.length());
        for (int k = 0; k < b.length(); k++) {
            double w = (robustnessDataG ? w2(k) : w(k));
            double s = 0;
            for (int i = 0; i < F.length(); i++) {
                for (int j = 0; j < ln_t.length; j++) {
                    if (Z(i, j) == k) {
                        double t = F.get(i) + ln_t[j];
                        if (robustnessDataG) {
                            t /= phi_data[i][j];
                        }
                        s += t;
                    }
                }
            }
            b.set(k, s * w);
        }
        return b;
    }

    private DecimalVector calculateF(DecimalVector g, DecimalVector F, int i) {
        int MAX_ITERATIONS = 1;
        if (!robustnessDataG) {
            MAX_ITERATIONS = GLOBAL_INNER_ITERATIONS;
        }
        for (int it = 0; it < MAX_ITERATIONS; it++) {
            if (robustnessDataG) {
                update_phi_smooth(g);
                update_phi_data(g, F);
            }
            if (alpha > 0)
                F = solveFWithNeighbors(g, F, alpha);
            else
                F = solveFDefault(g, F);
        }
        return F;
    }

    private DecimalVector solveFDefault(DecimalVector g, DecimalVector f) {
        double quot;
        double div;
        double t;
        for (int i = 0; i < N; i++) {
            quot = 0;
            div = 0;
            for (int j = 0; j < P; j++) {
                if (robustnessDataG) {
                    // Zähler
                    t = w2(Z(i, j)) / phi_data[i][j] * (g.get(Z(i, j)) - ln_t[j]);
                    quot += t;
                    // Nenner
                    t = w2(Z(i, j)) / phi_data[i][j];
                    div += t;
                } else {
                    quot += g.get(Z(i, j)) - ln_t[j] * w(Z(i, j));
                    div += w(Z(i, j));
                }
            }
            f.set(i, quot / div);
        }
        return f;
    }

    private DecimalVector solveFWithNeighbors(DecimalVector g, DecimalVector oldF, double alpha) {
        return getNeighborMatrix(g, oldF, alpha);
    }


    @Override
    protected HDRResult doInBackground() throws Exception {
        // start value for g, lets assume we just use a linear equotation
        long started = System.currentTimeMillis();
        DecimalVector g = new DecimalVector(256);
        DecimalVector F = new DecimalVector(N);

        final double[] energy = new double[iterations / energySteps];
        g = initG(g);


        double[] w = new double[256];
        for (int i = 0; i < w.length; i++) {
            w[i] = w(i);
        }
        for (int i = 0; i < F.length(); i++)
            F.set(i, 1);
        ScatterPlot enPlot = new ScatterPlot(energy);
        Controller.getInstance().getDisplay().addPlot(enPlot, "Energieverlauf");

        for (int i = 0; i < iterations && !isCancelled(); i++) {
            setProgress(100 * i / iterations);
            F = calculateF(g, F, i);
            setProgress(getProgress() + 100 / (iterations * 2));
            g = calculateG(F, g, i);

            //if (i % energySteps == 0) {
            publish(new HDRResult(F.exp(), g, images.get(0).getWidth(), images.get(0).getHeight()));
            try {
                energy[i / energySteps] = calculateEnergy(F, g);
            } catch (Exception e) {
                System.out.println("Did not add energy");
            }
            enPlot.setY(energy);
            //}
        }
        setProgress(100);
        long finished = System.currentTimeMillis();
        Controller.getInstance().getDisplay().append("Took: " + (finished - started) / 1000.0 / 60.0 + "min");
        return new HDRResult(F.exp(), g, images.get(0).getWidth(), images.get(0).getHeight());
    }


    private DecimalVector initG(DecimalVector g) {
        for (int i = 0; i < g.length(); i++)
            g.set(i, -5 + i * 5.0 / 127.0);
        return g;
    }


    protected void update_phi_data(DecimalVector g, DecimalVector F) {
        if (robustnessDataG) {
            for (int i = 0; i < F.length(); i++) {
                for (int j = 0; j < ln_t.length; j++) {
                    phi_data[i][j] = Math.sqrt(Math.pow(g.get(Z(i, j)) - F.get(i) - ln_t[j], 2) + EPSILON_2);
                }
            }
        }
    }

    protected void update_phi_smooth(DecimalVector g) {
        if (robustnessSmoothnessG) {
            // set boundaries (not required, just to make a well defined state)
            phi_smooth[0] = 1;
            phi_smooth[phi_smooth.length - 1] = 1;
            for (int k = 1; k < phi_smooth.length - 1; k++) {
                double v = g.get(k - 1) - 2 * g.get(k) + g.get(k + 1);
                phi_smooth[k] = Math.sqrt(Math.pow(v, 2) + EPSILON_2);
            }
        }
    }

    protected void process(List<HDRResult> pairs) {
        DecimalVector g = pairs.get(0).getG();
        DecimalVector E = pairs.get(0).getE();
        Controller.getInstance().getDisplay().addPlot(new ScatterPlot(g), "g(" + getProgress() + "%)");
        //Controller.getInstance().getDisplay().addPlot(new ScatterPlot(E), "E(" + getProgress() + "%)");
    }


    /**
     * calculates the Energy of a entire Picture set for given irradiance values E[0..N-1] and a given
     * function g where g is specified as vector (the entry i in the vector represents the value for the function g(i) )
     *
     * @param F - the log of the irradiance values 0..N-1 (N is the picture size) (log(Ei)!!!)
     * @param g - the function g (the entry i in the vector represents the value of g(i) )
     * @return the value of the energy functional  SUM(i=1,N,SUM(j=1,P,[g(Z_ij)-ln(E_i)-ln(dt_j)]^2))+lambda*SUM(z=1,254,g''(z)^2)
     */
    public double calculateEnergy(DecimalVector F, DecimalVector g) {


        // data term
        double data = 0.0;
        for (int i = 0; i < F.length(); i++) {
            for (int j = 0; j < images.size(); j++) {
                // w^2(Z(i,j))*phi([g(Z(i,j))-ln(E_i)-ln(t_j)]^2)
                double w = w2(Z(i, j));
                double inner = g.get(Z(i, j)) - (F.get(i)) - ((ln_t[j]));
                double phi = Math.sqrt(Math.pow(inner, 2) + EPSILON_2);
                data += w * phi;
            }
        }
        // smoothning term
        double smoothing = 0.0;
        for (int z = 1; z < 255; z++) {
            double g2 = g.get(z - 1) - 2 * g.get(z) + g.get(z + 1);
            double phi = Math.sqrt(Math.pow(g2, 2) + EPSILON_2);
            smoothing += w2(z) * phi;
        }
        // monotonie term
        double monotonie = 0.0;
        for (int z = 1; z <= 255; z++) {
            // w(z)*(g'(z)<0 * g'(z))^2
            double g1 = g.get(z - 1) - (g.get(z));
            if (g1 < 0)
                monotonie += w(z) * g1 * g1;
        }

        System.out.println("Energy " + data + ", " + lambda * smoothing + ", " + mu * monotonie);
        return data + lambda * smoothing + mu * monotonie;

    }


    @Override
    public String toString() {
        String s = "Iterativ Energy Solver:\n";
        s += " Monotonie:            " + (mu > 0 ? mu : "deaktiviert") + "\n";
        s += " Smoothness:           " + lambda + "\n";
        s += " Iterationen:          " + iterations + "\n";
        s += " Images:               " + P + "\n";
        s += " Image-Size:           " + N + "\n";
        s += " Weight-Mode:          " + weightMode.toString() + "\n";
        s += " Räumliche Glattheit:  " + (alpha > 0 ? alpha : "deaktiviert") + "\n";
        s += " Robustheit Datenterm g:" + (robustnessDataG ? "aktiv" : "deaktiv") + "\n";
        s += " Robustheit Glattheit g:" + (robustnessSmoothnessG ? "aktiv" : "deaktiv") + "\n";

        return s;
    }


    public DecimalVector getNeighborMatrix(DecimalVector g, DecimalVector F, double alpha) {
        int cols = images.get(0).getWidth();
        int rows = images.get(0).getHeight();

        BandMatrix res = generateNeighborsBandMatrix(alpha, cols, rows);
        DecimalVector b = new DecimalVector(F.length());
        for (int i = 0; i < b.length(); i++) {
            double sum = 0;
            for (int j = 0; j < P; j++) {
                sum += w(Z(i, j)) * (g.get(Z(i, j)) - ln_t[j]);
            }
            b.set(i, sum);
        }
        return res.solveSOR(b, F);
    }


    private BandMatrix neighborsBandMatrix = null;

    private BandMatrix generateNeighborsBandMatrix(double alpha, int cols, int rows) {
        if (neighborsBandMatrix == null) {
            neighborsBandMatrix = new BandMatrix(cols * rows, new int[]{-cols, -1, 0, 1, cols});
            for (int i = 0; i < cols * rows; i++) {
                int d = -4;
                if (i / cols == 0 || i / cols == rows - 1)
                    d += 1;
                if (i % cols == 0 || (i + 1) % cols == 0)
                    d += 1;

                // weight
                double sum = 0;
                for (int j = 0; j < P; j++) {
                    sum += w(Z(i, j));
                }
                neighborsBandMatrix.set(i, i, sum - alpha * d);
                // left band
                if (i - 1 >= 0)
                    neighborsBandMatrix.set(i - 1, i, -1 * alpha);
                // right band
                if (i + 1 < cols * rows)
                    neighborsBandMatrix.set(i + 1, i, -1 * alpha);
                // upper band
                if (i - cols >= 0)
                    neighborsBandMatrix.set(i - cols, i, -1 * alpha);
                // lower band
                if (i + cols < cols * rows)
                    neighborsBandMatrix.set(i + cols, i, -1 * alpha);
            }
        }
        return neighborsBandMatrix;
    }
}
