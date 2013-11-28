package Maths;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.MathContext;
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
public abstract class Matrix {
    public static MathContext c = MathContext.DECIMAL32;
    protected Matrix R = null;
    protected Matrix L = null;

    public abstract Matrix mult(double a);

    public abstract void set(int i, int j, double f);

    public abstract int cols();

    public abstract int rows();

    public abstract double get(int row, int col);


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


    public Matrix mult(Matrix m) {
        if (m.rows() != cols())
            throw new IllegalArgumentException("Not the same rows and colls. Inputs has " + m.rows() + " rows. I have " + cols() + " cols");
        Matrix c = new DefaultMatrix(rows(), m.cols());
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

    public Matrix add(Matrix m) {
        if (m.cols() != rows() || m.cols() != cols())
            throw new IllegalArgumentException("Not the same rows and colls");
        DefaultMatrix r = new DefaultMatrix(rows(), cols());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                r.set(i, j, m.get(i, j) + get(i, j));
            }
        }
        return r;
    }

    public Matrix transpose() {
        Matrix res = new DefaultMatrix(cols(), rows());
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                res.set(c, r, get(r, c));
            }
        }
        return res;
    }


    public Matrix[] decomposePenta() {
        if (L == null || R == null) {
            final int n = rows();
            L = new DefaultMatrix(n);
            R = new DefaultMatrix(n);
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

        return new Matrix[]{L, R};
    }

    /**
     * solves a linear equotation system of the kind
     * A * x = b
     * where A is the matrix itself (this) and x is the result of the function.
     * The Matrix A has to be in pentadiagonal structure (so only the 5 center diagonales
     * are allowed to have values).
     *
     * @param b the vector on the right sight of the LGS
     * @return the vector x.
     */
    public Vector solvePentadiagonale(Vector b) {
        checkPentadiagonale();
        Matrix[] LU = decomposePenta();
        // forward elimination L*y = b
        Vector y = forwardElimination(b, LU[0]);
        // backward substitution U*g = y
        return backwardSubstitution(LU[1], y);
    }

    public static Vector forwardElimination(Vector b, Matrix l) {
        Vector y = new Vector(b.length());
        y.set(0, b.get(0));
        y.set(1, b.get(1) - l.get(1, 0) * y.get(0));
        for (int i = 2; i < b.length(); i++) {
            y.set(i, b.get(i) - l.get(i, i - 2) * y.get(i - 2) - l.get(i, i - 1) * y.get(i - 1));
        }
        return y;
    }

    public static Vector backwardSubstitution(Matrix U, Vector y) {
        int n = y.length();
        Vector g = new Vector(n);
        g.set(n - 1, y.get(n - 1) / U.get(n - 1, n - 1));
        g.set(n - 2, (y.get(n - 2) - U.get(n - 2, n - 1) * g.get(n - 1)) / U.get(n - 2, n - 2));
        for (int i = n - 3; i >= 0; i--) {
            double d = U.get(i, i);
            double v = U.get(i, i + 1) * g.get(i + 1) / d;
            double v1 = U.get(i, i + 2) * g.get(i + 2) / d;
            double v2 = y.get(i) / d;
            g.set(i, v2 - v - v1);
        }
        return g;
    }

    private void checkPentadiagonale() {
        // Penta diagonale check
        for (int i = 0; i < cols(); i++) {
            for (int j = 0; j < rows(); j++) {
                if ((i < j - 2 || i > j + 2) && Math.abs(get(j, i)) > 0.0000001) {
                    throw new ArithmeticException("This Matrix is not pentadiagonale in " + i + "|" + j + " with " + get(j, i));
                }
            }
        }
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

    public Matrix clone() {
        Matrix r = new DefaultMatrix(rows(), cols());
        for (int row = 0; row < rows(); row++) {
            for (int col = 0; col < cols(); col++) {
                r.set(row, col, get(row, col));
            }
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Matrix) {
            Matrix m = (Matrix) o;
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

    public static Matrix parse(String s) {
        String[] lines = s.trim().split("\n");
        int rows = lines.length;
        int cols = lines[0].trim().split(" ").length;

        Matrix m = new DefaultMatrix(rows, cols);
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
                Matrix.this.toFileSync(filename);
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
}
