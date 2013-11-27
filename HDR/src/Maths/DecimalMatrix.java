package Maths;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 12.07.13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public class DecimalMatrix extends Matrix {

    double[][] mat;

    public DecimalMatrix(int n) {
        this(n, n);
    }


    public DecimalMatrix(int m, int n) {
        mat = new double[m][n];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                mat[i][j] = 0;
            }
        }
    }

    public DecimalMatrix(short[][] doubles) {
        this(doubles.length, doubles[0].length);
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                mat[i][j] = (doubles[i][j]);
            }
        }
    }

    public DecimalMatrix(int[][] doubles) {
        this(doubles.length, doubles[0].length);
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                mat[i][j] = (doubles[i][j]);
            }
        }
    }

    public DecimalMatrix(double[][] doubles) {
        this(doubles.length, doubles[0].length);
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                mat[i][j] = (doubles[i][j]);
            }
        }
    }


    @Override
    public DecimalMatrix mult(double a) {
        DecimalMatrix m = new DecimalMatrix(rows(), cols());
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                m.set(i, j, mat[i][j] * (a));
            }
        }
        return m;
    }

    @Override
    public void set(int i, int j, double f) {
        if (Double.isNaN(f) || Double.isInfinite(f)) {
            throw new ArithmeticException("Value is " + f);
        }
        if (i >= 0 && j >= 0 && i < mat.length && j < mat[i].length) {
            mat[i][j] = f;
            L = null;
            R = null;
        } else {
            throw new IndexOutOfBoundsException("cannot set " + i + "|" + j + ": index out of range");
        }
    }

    @Override
    public int cols() {
        return mat[0].length;
    }

    @Override
    public int rows() {
        return mat.length;
    }

    @Override
    public double get(int row, int col) {
        return mat[row][col];

    }
}