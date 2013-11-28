package Maths;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This implements a band Matrix with diagonales from top left to bottom right.
 * It can be used to save memory.
 *
 * @author sebastianzillessen
 */
public class BandMatrix extends Matrix {


    private final int MAX_ITERATIONS_SOR = 100;
    //If we only have a change of 1 promille it is already fine!
    private final double ACCEPTED_DIFFERENCE_SOR = 0.001;
    //less then 2 percent improvement is already done ;)
    private final double ACCEPTED_PERCENTAGE_RESIDUUM_SOR = 0.02;
    private final double OMEGA_SOR = 1.2;
    protected double[] elements;
    protected int[] bandIndexes;
    private int size;

    /**
     * creates a new BandMatrix out of an existing matrix (can sometimes be good for transformations)
     *
     * @param m
     */
    public BandMatrix(Matrix m) {
        this(m.cols(), extractBands(m));
        // init values
        for (int row = 0; row < rows(); row++) {
            for (int i = 0; i < bandIndexes.length; i++) {
                int col = bandIndexes[i] + row;
                if (col >= 0 && col < size && m.get(row, col) != 0) {
                    set(row, col, m.get(row, col));
                }
            }
        }
    }

    /**
     * generates a new bandmatrix of the size size x size with the specified band indexes.
     * 0 is the center diagonale from top left to bottom right.
     * -n is the n-th diagonale below the center.
     * +n is the n-th diagonele above the center.
     *
     * @param size        size of the matrix
     * @param bandIndexes diagonales which should be able to be set (0 is zenter)
     */
    public BandMatrix(int size, int[] bandIndexes) {
        this(size);
        Arrays.sort(bandIndexes);
        int elementSize = 0;
        for (int i = 0; i < bandIndexes.length; i++) {
            elementSize += (size - Math.abs(bandIndexes[i]));
        }
        this.elements = new double[elementSize];
        for (int i = 0; i < elementSize; i++) {
            elements[i] = 0;
        }
        this.bandIndexes = bandIndexes;
    }

    /**
     * generates a new bandmatrix of the size size x size with the specified band indexes.
     * 0 is the center diagonale from top left to bottom right.
     * -n is the n-th diagonale below the center.
     * +n is the n-th diagonele above the center.
     *
     * @param size        size of the matrix
     * @param bandIndexes diagonales which should be able to be set (0 is zenter)
     */
    public BandMatrix(int size, Set bandIndexes) {
        this(size, ArrayMaths.TointArray(bandIndexes));
    }


    /**
     * Parses a String to a Band Matrix. Entries have to be seperated by space, new lines represent a new row in the matrix.
     * <p/>
     * Example:
     * <p/>
     * s = "1 2 0 0\n"+
     * "0 7 8 0 \n"+
     * "0 0 1 1"+
     *
     * @param s space seperated matrix input (new lines for new row)
     * @return a band matrix corresponding to s
     */
    public static BandMatrix parse(String s) {
        return new BandMatrix(Matrix.parse(s));
    }

    @Override
    public BandMatrix add(Matrix m) {

        if (m.cols() != size || m.rows() != size)
            throw new IllegalArgumentException("The both matrices to add are not of the same size");
        Set indexes = new HashSet<Integer>();
        for (int row = 0; row < m.rows(); row++) {
            for (int col = 0; col < m.cols(); col++) {
                if (m.get(row, col) != 0)
                    indexes.add(col - row);
            }
        }
        BandMatrix res = new BandMatrix(size, indexes);
        for (int row = 0; row < size; row++) {
            for (int i = 0; i < res.bandIndexes.length; i++) {
                int col = row + res.bandIndexes[i];
                if (col >= 0 && col < size && getIndex(row, col) != -1)
                    res.set(row, col, this.get(row, col) + (m.get(row, col)));
            }
        }
        return res;
    }

    public BandMatrix add(BandMatrix m) {
        if (m.size != size) {
            throw new IllegalArgumentException("The both matrices to add are not of the same size");
        }
        // get indexes
        Set indexes = new HashSet<Integer>();
        for (int i = 0; i < bandIndexes.length; i++) {
            indexes.add(bandIndexes[i]);
        }
        for (int i = 0; i < m.bandIndexes.length; i++) {
            indexes.add(m.bandIndexes[i]);
        }

        BandMatrix res = new BandMatrix(size, indexes);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double n = 0;
                if (m.get(row, col) != 0)
                    n += m.get(row, col);
                if (get(row, col) != 0)
                    n += get(row, col);
                if (n != 0)
                    res.set(row, col, n);
            }
        }
        return res;
    }

    @Override
    public BandMatrix clone() {
        BandMatrix r = new BandMatrix(size, bandIndexes);
        for (int i = 0; i < elements.length; i++) {
            r.elements[i] = elements[i];
        }
        return r;
    }

    @Override
    public BandMatrix mult(double a) {
        BandMatrix m = new BandMatrix(size, bandIndexes);
        for (int i = 0; i < elements.length; i++) {
            m.elements[i] = elements[i] * a;
        }
        return m;
    }

    @Override
    public void set(int row, int col, double f) {
        if (Double.isInfinite(f) || Double.isNaN(f)) {
            throw new ArithmeticException("Value to set in Band Matrix is " + f);
        }
        int pos = getIndex(row, col);
        if (pos == -1 || pos >= elements.length) {
            // give only an alert if i'm not zero
            if (f != 0.0) {
                String s = "";
                for (int i = 0; i < bandIndexes.length; i++) {
                    s += bandIndexes[i] + ",";
                }
                throw new IndexOutOfBoundsException("This is a Band Matrix and you tried to set a value which is not in the bounds (" + row + "|" + col + ")\n" +
                        " The Matrix diagonales are: " + s);
            }
        } else
            elements[pos] = f;
    }

    @Override
    public int cols() {
        return size;
    }

    @Override
    public int rows() {
        return size;
    }

    public double get(int row, int col) {
        int pos = getIndex(row, col);
        if (pos == -1)
            return 0.0;
        else {
            return elements[pos];

        }
    }

    public String debug() {
        String s = "";
        for (int i = 0; i < bandIndexes.length; i++) {
            s += bandIndexes[i] + ",";
        }
        s = "This is a Band Matrix :\n" +
                " The Matrix diagonales are: " + s + "\n" +
                " This Matrix has only " + elements.length + " elements.\n" +
                " The matrix is: " + rows() + "x" + cols() + "\n" +
                " The Matrix elements are: ";
        for (int i = 0; i < elements.length; i++) {
            s += elements[i] + ",";
        }
        s += "\n";
        return s;


    }

    public Vector solveSOR(Vector b) {
        return solveSOR(b, OMEGA_SOR, ACCEPTED_PERCENTAGE_RESIDUUM_SOR, ACCEPTED_DIFFERENCE_SOR);
    }

    public Vector solveSOR(Vector right, Vector init) {
        return solveSOR(right, OMEGA_SOR, ACCEPTED_PERCENTAGE_RESIDUUM_SOR, ACCEPTED_DIFFERENCE_SOR, init);
    }

    public Vector solveSOR(Vector right, double omega, double accepted_error, double accepted_diff) {
        double[] x = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = 1;
        }
        return solveSOR(right, omega, accepted_error, accepted_diff, new Vector(x));
    }

    public Vector solveSOR(Vector b, double omega, double accepted_error, double accepted_diff, Vector x) {
        if (x.length() != size || b.length() != size)
            throw new IllegalArgumentException(String.format("The Vector are of wrong size. Expected: %d, Got for right side (b): %d, and for init: %d", size, b.length(), x.length()));


        Vector d = solveSORDouble(b, omega, accepted_error, accepted_diff, x, MAX_ITERATIONS_SOR);
        return d;
    }

    private Vector solveSORDouble(Vector bV, double omega, double accepted_error, double accepted_diff, Vector xV, int MAX_ITERATIONS) {
        checkSymmetric();
        checkPositiveSemiDefinit();
        double b[] = bV.toArray();
        double x[] = xV.toArray();
        // abbruch kriteriumg von http://www.home.hs-karlsruhe.de/~weth0002/buecher/mathe/downloads/kap21.pdf, S 143
        double first_res = -1;
        double old_x[] = new double[x.length];
        for (int iterations = 0; iterations <= MAX_ITERATIONS; iterations++) {
            for (int row = 0; row < size; row++) {
                double phi = 0;
                for (int i = 0; i < bandIndexes.length; i++) {
                    int col = row + bandIndexes[i];
                    if (col >= 0 && col < size && col != row) {
                        phi += get(row, col) * x[col];
                    }
                }
                x[row] = x[row] + omega * ((b[row] - phi) / get(row, row) - x[row]);
            }

            if (iterations % 10 == 0) {
                // Abbruch?
                try {
                    double max_abs_res = this.mult(x).subtract(bV).absMax();
                    if (first_res == -1)
                        first_res = max_abs_res;
                    max_abs_res /= first_res;
                    double max_diff = ArrayMaths.diffMax(x, old_x);
                    //System.out.printf(" %3d Max-Abs-res: %7.5f  Diff-Max: %7.5f%n", iterations, max_abs_res, max_diff);
                    if (max_abs_res < accepted_error && max_diff < accepted_diff) {
                        //System.out.println("-> Accuracy matched.");
                        break;
                    }
                } catch (ArithmeticException e) {
                    System.out.println("We have an error in calculation of the iteration criteria. Do not stop");
                }
            }
            old_x = Arrays.copyOf(x, x.length);
        }
        return new Vector(x);
    }

    /**
     * Checks if this matrix is symmetrix
     *
     * @return true if the matrix is symmetric. False otherwise.
     */
    public boolean isSysmmetric() {
        for (int i = 0; i < this.bandIndexes.length / 2; i++) {
            if (bandIndexes[i] != -bandIndexes[bandIndexes.length - 1 - i]) {
                System.out.println(bandIndexes[i] + "!=" + bandIndexes[bandIndexes.length - 1 - i]);
                return false;
            }
        }
        for (int row = 0; row < size; row++) {
            for (int i = 0; i < this.bandIndexes.length / 2; i++) {
                int col = row + bandIndexes[i];
                if (col >= 0 && col < size / 2 && get(row, col) != get(col, row)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BandMatrix transpose() {
        int[] newBandIndexes = Arrays.copyOf(bandIndexes, bandIndexes.length);
        for (int i = 0; i < bandIndexes.length; i++)
            newBandIndexes[i] = -1 * bandIndexes[i];
        BandMatrix res = new BandMatrix(size, newBandIndexes);
        for (int row = 0; row < size; row++) {
            for (int k = 0; k < newBandIndexes.length; k++) {
                int col = row + newBandIndexes[k];
                if (col >= 0 && col < size)
                    res.set(row, col, get(col, row));
            }
        }
        return res;
    }

    /**
     * Multiplies to Band-Matrices in an efficient way.
     * <p/>
     * If no efficient way is found it falls back to default multiplication and parses it as Band Matrix.
     *
     * @param m other band matrix
     * @return Result of this * m as matrix multiplication
     * @see Matrix#mult(Matrix)
     */
    public BandMatrix mult(BandMatrix m) {
        // Multiply column wise, because second matrix is only middle diagonale
        if (m.size == this.size && m.bandIndexes.length == 1 && m.bandIndexes[0] == 0) {
            BandMatrix res = this.clone();
            for (int col = 0; col < size; col++) {
                double f = m.get(col, col);
                for (int row = 0; row < size; row++) {
                    res.set(row, col, res.get(row, col) * (f));
                }
            }
            return res;
        }    // Multiply row wise, because first matrix is only middle diagonale
        else if (m.size == this.size && bandIndexes.length == 1 && bandIndexes[0] == 0) {
            BandMatrix res = new BandMatrix(size, m.bandIndexes);
            for (int row = 0; row < size; row++) {
                double f = this.get(row, row);
                for (int i = 0; i < m.bandIndexes.length; i++) {
                    int col = m.bandIndexes[i] + row;
                    if (col >= 0 && col < size) {
                        res.set(row, col, m.get(row, col) * (f));
                    }
                }
            }
            return res;
        } else {
            return new BandMatrix(super.mult(m));
        }

    }

    @Override
    public Vector mult(final Vector x) {
        if (x.length() != this.cols())
            throw new IllegalArgumentException("Matrix * vector: vector must be of size " + cols() + " but was " + x.length());
        final Vector r = new Vector(this.rows());

        ExecutorService executor = Executors.newFixedThreadPool(7);
        for (int i = 0; i < rows(); i++) {
            final int finalI = i;
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    double sum = 0;
                    for (int j = 0; j < bandIndexes.length; j++) {
                        int k = finalI + bandIndexes[j];
                        if (k >= 0 && k < cols() && x.get(finalI) != 0 && get(finalI, k) != 0)
                            sum += get(finalI, k);
                    }
                    r.set(finalI, sum * x.get(finalI));
                }
            };
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return r;
    }

    public boolean isPositivSemiDefinite() {
        for (int row = 0; row < size; row++) {
            double sum = 0;
            for (int i = 0; i < bandIndexes.length; i++) {
                int col = row + bandIndexes[i];
                if (row != col && col < size && col >= 0)
                    sum = sum + (get(row, col));
            }
            if (sum > get(row, row)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean toFileSync(String filename) {
        BufferedWriter writer = null;
        boolean success = false;
        try {
            writer = new BufferedWriter(new FileWriter(filename));

            for (int row = 0; row < rows(); row++) {
                for (int i = 0; i < bandIndexes.length; i++) {
                    int col = row + bandIndexes[i];
                    if (col >= 0 && col < cols()) {
                        writer.append(String.format(Locale.ENGLISH, "%8.4f", get(row, col)));
                    } else {
                        writer.append("        ");
                    }
                    if (col < cols() - 1)
                        writer.append(" ");
                }
                writer.append("\n");
            }
            writer.append("]\n");
            writer.flush();
            System.out.println("File '" + filename + "' saved successfully.");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
        return success;

    }

    private static int[] extractBands(Matrix m) {
        Set<Integer> diagonales = new HashSet<Integer>();
        if (m.rows() != m.cols())
            throw new IllegalArgumentException("Only Quadratic Matrices allowed");
        for (int row = 0; row < m.rows(); row++) {
            for (int col = 0; col < m.cols(); col++) {
                double v = m.get(row, col);
                if (v != 0) {
                    diagonales.add(col - row);
                }
            }
        }
        return ArrayMaths.TointArray(diagonales);
    }

    private BandMatrix(int n) {
        this.size = n;
    }

    /**
     * checks if the matrix is symmetric and throws an error if it is not.
     */
    private void checkSymmetric() {
        if (!isSysmmetric())
            throw new IllegalArgumentException("Matrix is not symmetric.");
    }

    /**
     * checks if the matrix is positiv semi-definit and throws an error if it is not.
     */
    private void checkPositiveSemiDefinit() {
        if (!isPositivSemiDefinite())
            throw new IllegalArgumentException("Matrix is not positive definite");
    }

    /**
     * returns the index in the internal array of a given row and col
     *
     * @param row row in the matrix
     * @param col col in the matrix
     * @return index in the band index array (1D) or -1 if not availble
     */
    private int getIndex(int row, int col) {
        int diffToCenterInRow = col - row;
        int pos = Arrays.binarySearch(bandIndexes, diffToCenterInRow);
        if (pos < 0)
            return -1;
        else {
            int index = row;
            for (int i = 0; i < pos; i++) {
                index += (size - Math.abs(bandIndexes[i]));
            }
            if (row > col)
                index += bandIndexes[pos];

            return index;
        }
    }
}
