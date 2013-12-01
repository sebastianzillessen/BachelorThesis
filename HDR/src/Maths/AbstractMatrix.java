package Maths;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 03.11.13
 * Time: 17:32
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMatrix {
    /* Abstract methods to overwrite */

    public abstract void set(int i, int j, double f);


    public abstract double get(int row, int col);

    public abstract int cols();

    public abstract int rows();

    public abstract String debugString();

    public abstract AbstractMatrix mult(double a);


    /**
     * Variables *
     */
    protected AbstractMatrix R = null;
    protected AbstractMatrix L = null;


    public Vector mult(final Vector x) {
        if (x.length() != cols())
            throw new IllegalArgumentException("Matrix * vector: vector must be of size " + cols() + " but was " + x.length());
        final Vector r = new Vector(this.rows());
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (int i = 0; i < rows(); i++) {
            final int finalI = i;
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    double sum = 0;
                    for (int j = 0; j < cols(); j++) {
                        if (x.get(j) != 0 && get(finalI, j) != 0)
                            sum += x.get(j) * get(finalI, j);
                    }
                    r.set(finalI, sum);
                }
            };
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        return r;
    }

    public Vector mult(double[] x) {
        return this.mult(new Vector(x));
    }


    public AbstractMatrix mult(AbstractMatrix m) {
        if (m.rows() != cols())
            throw new IllegalArgumentException("Not the same rows and colls. Inputs has " + m.rows() + " rows. I have " + cols() + " cols");
        AbstractMatrix c = new Matrix(rows(), m.cols());
        for (int i = 0; i < c.rows(); i++) {
            for (int k = 0; k < c.cols(); k++) {
                double a = 0;
                for (int j = 0; j < cols(); j++) {
                    if (get(i, j) != 0 && m.get(j, k) != 0)
                        a += get(i, j) * m.get(j, k);
                }
                c.set(i, k, a);
            }
        }

        return c;
    }

    public AbstractMatrix add(AbstractMatrix m) {
        if (m.cols() != rows() || m.cols() != cols())
            throw new IllegalArgumentException("Not the same rows and colls");
        Matrix r = new Matrix(rows(), cols());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                r.set(i, j, m.get(i, j) + get(i, j));
            }
        }
        return r;
    }

    public AbstractMatrix transpose() {
        AbstractMatrix res = new Matrix(cols(), rows());
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                res.set(c, r, get(r, c));
            }
        }
        return res;
    }


    public AbstractMatrix[] decomposePenta() {
        if (L == null || R == null) {
            final int n = rows();
            L = new Matrix(n);
            R = new Matrix(n);
            double[] m = new double[n];
            double[] l = new double[n];
            double[] k = new double[n];
            double[] p = new double[n];
            double[] r = new double[n];
            // initialization
            m[0] = get(0, 0);
            r[0] = get(0, 1);
            l[1] = get(1, 0) / m[0];
            m[1] = get(1, 1) - l[1] * r[0];

            // p_i s
            for (int i = 0; i < n - 2; i++) {
                p[i] = get(i, i + 2);
            }

            for (int i = 2; i < n; i++) {
                k[i] = get(i, i - 2) / m[i - 2];
                l[i] = (get(i, i - 1) - k[i] * r[i - 2]) / m[i - 1];
                r[i - 1] = get(i - 1, i) - (l[i - 1] * (p[i - 2]));
                m[i] = get(i, i) - k[i] * p[i - 2] - l[i] * r[i - 1];
            }

            // build L
            for (int i = 0; i < n; i++) {
                L.set(i, i, 1);
                if (i >= 1)
                    L.set(i, i - 1, l[i]);
                if (i >= 2)
                    L.set(i, i - 2, k[i]);
            }

            //build R
            for (int i = 0; i < n; i++) {
                R.set(i, i, m[i]);
                if (i < n - 1)
                    R.set(i, i + 1, r[i]);
                if (i < n - 2)
                    R.set(i, i + 2, p[i]);

            }
        }

        return new AbstractMatrix[]{L, R};
    }


    @Override
    public String toString() {
        String s = "[\n";
        for (int row = 0; row < rows(); row++) {
            for (int col = 0; col < cols(); col++) {
                s += String.format(Locale.ENGLISH, "%8.4f", get(row, col));
                if (col < cols() - 1)
                    s += " ";
            }
            s += "\n";
        }
        s += "]\n";
        return s;
    }

    public AbstractMatrix clone() {
        AbstractMatrix r = new Matrix(rows(), cols());
        for (int row = 0; row < rows(); row++) {
            for (int col = 0; col < cols(); col++) {
                r.set(row, col, get(row, col));
            }
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractMatrix) {
            AbstractMatrix m = (AbstractMatrix) o;
            if (m.rows() == rows() && m.cols() == cols()) {
                for (int i = 0; i < rows(); i++)
                    for (int j = 0; j < cols(); j++)
                        if (get(i, j) != m.get(i, j))
                            return false;
                return true;
            }
        }
        return false;
    }

    public static AbstractMatrix parse(String s) {
        String[] lines = s.trim().split("\n");
        int rows = lines.length;
        int cols = lines[0].trim().split(" ").length;

        AbstractMatrix m = new Matrix(rows, cols);
        for (int row = 0; row < rows; row++) {
            String[] rowElements = lines[row].trim().split(" ");
            for (int col = 0; col < cols; col++) {
                double v = Double.parseDouble(rowElements[col]);
                if (v != 0.0) {
                    m.set(row, col, v);
                }
            }
        }
        return m;
    }

    public void toFile(final String filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AbstractMatrix.this.toFileSync(filename);
            }
        }).start();
    }

    public double[][] toArray() {
        double[][] res = new double[rows()][cols()];
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                res[i][j] = get(i, j);
            }
        }
        return res;
    }

    public boolean toFileSync(String filename) {
        BufferedWriter writer = null;
        boolean success = false;
        try {
            writer = new BufferedWriter(new FileWriter(filename));

            writer.append("[\n");
            for (int row = 0; row < rows(); row++) {
                for (int col = 0; col < cols(); col++) {
                    writer.append(String.format(Locale.ENGLISH, "%8.4f", get(row, col)));
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
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
        return success;

    }

    public boolean isQuadratic() {
        return rows() == cols();
    }

    public boolean isSymmetric() {
        if (!isQuadratic())
            return false;
        for (int row = 0; row < rows(); row++) {
            for (int col = 0; col < cols(); col++) {
                if (get(row, col) != get(col, row)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isPositiveSemiDefinit() {
        for (int row = 0; row < rows(); row++) {
            double sum = 0;
            for (int col = 0; col < cols(); col++) {
                sum = sum + (get(row, col));
            }
            if (sum > get(row, row)) {
                return false;
            }
        }
        return true;
    }

    public boolean isPentadiagonale() {
        for (int row = 0; row < rows(); row++) {
            for (int col = 0; col < cols(); col++) {
                if ((col < row - 2 || col > row + 2) && get(row, col) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}