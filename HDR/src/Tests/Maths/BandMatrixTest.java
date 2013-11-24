package Tests.Maths;

import Maths.DecimalMatrix;
import Maths.DecimalVector;
import Maths.Matrix;
import Maths.BandMatrix;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 13.07.13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class BandMatrixTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testMult() throws Exception {

    }

    @Test
    public void testMultDouble() throws Exception {

    }

    @Test
    public void testSetDouble() throws Exception {

    }

    @Test
    public void testCols() throws Exception {

    }

    @Test
    public void testRows() throws Exception {

    }

    private BandMatrix m(int n) {
        BandMatrix d = new BandMatrix(n, new int[]{-2, -1, 0, 1, 2});
        int[] basis = new int[]{1, -4, 6, -4, 1};
        // first row
        d.set(0, 0, 1);
        d.set(0, 1, -2);
        d.set(0, 2, 1);
        // second row
        d.set(1, 0, -2);
        d.set(1, 1, 5);
        d.set(1, 2, -4);
        d.set(1, 3, 1);
        // diagonale
        for (int row = 2; row < n - 2; row++) {
            // default matrixG 2nd degree on the base.
            for (int c = 0; c < 5; c++) {
                d.set(row, row + (c - 2), basis[c]);
            }
        }
        // second last row
        d.set(n - 2, n - 4, 1);
        d.set(n - 2, n - 3, -4);
        d.set(n - 2, n - 2, 5);
        d.set(n - 2, n - 1, -2);

        // last row
        d.set(n - 1, n - 3, 1);
        d.set(n - 1, n - 2, -2);
        d.set(n - 1, n - 1, 1);
        for (int row = 0; row < n; row++) {
            d.set(row, row, d.get(row, row) + 1);
        }
        return d;
    }

    @Test
    public void testDecomposePenta() throws Exception {
        final int n = 7;
        BandMatrix d = m(n);
        Matrix[] ms = d.decomposePenta();
        Matrix L = ms[0];
        Matrix U = ms[1];


        Matrix eL = Matrix.parse("1 0 0 0 0 0 0\n" +
                "-1 1 0 0 0 0 0\n" +
                "0.5 -0.75 1 0 0 0 0\n" +
                "0 0.25 -0.7647059 1 0 0 0\n" +
                "0 0 0.2352941 -0.7586207 1 0 0\n" +
                "0 0 0 0.2344828 -0.7519999 1 0\n" +
                "0 0 0 0 0.2320000 -0.3750000 1");
        assertEquals(eL.toString(), L.toString());
        Matrix eR = Matrix.parse("2 -2 1 0 0 0 0\n" +
                "0 4 -3 1 0 0 0\n" +
                "0 0 4.25 -3.25 1 0 0\n" +
                "0 0 0 4.264706 -3.235294 1 0\n" +
                "0 0 0 0 4.310345 -3.241379 1\n" +
                "0 0 0 0 0 3.328000 -1.248000\n" +
                "0 0 0 0 0 0 1.300000");
        assertEquals(eR.toString(), U.toString());
    }


    @Test
    public void checkLUDecomp() {
        BandMatrix d = m(7);
        Matrix[] LU = d.decomposePenta();

        BandMatrix L = BandMatrix.parse(
                " 1 0 0 0 0 0 0\n" +
                        " -1 1 0 0 0 0 0\n" +
                        " 0.5000 -0.7500 1 0 0 0 0\n" +
                        " 0 0.2500 -0.7647 1 0 0 0\n" +
                        " 0 0 0.2353 -0.7586 1 0 0\n" +
                        " 0 0 0 0.2345 -0.7520 1 0\n" +
                        " 0 0 0 0 0.2320 -0.3750 1");
        BandMatrix U = BandMatrix.parse(" 2 -2 1 0 0 0 0\n" +
                " 0 4 -3 1 0 0 0\n" +
                " 0 0 4.2500 -3.2500 1 0 0\n" +
                " 0 0 0 4.2647 -3.2353 1 0\n" +
                " 0 0 0 0 4.3103 -3.2414 1 \n" +
                " 0 0 0 0 0 3.3280 -1.2480\n" +
                " 0 0 0 0 0 0 1.3000");

        assertEquals(L.toString(), LU[0].toString());
        assertEquals(U.toString(), LU[1].toString());
    }

    @Test
    public void TestForwardElimination() {
        DecimalVector b = new DecimalVector(new double[]{
                4, 5, 12, 3, 1, 2, 4
        });

        BandMatrix L = BandMatrix.parse(
                " 1 0 0 0 0 0 0\n" +
                        " -1 1 0 0 0 0 0\n" +
                        " 0.5000 -0.7500 1 0 0 0 0\n" +
                        " 0 0.2500 -0.7647 1 0 0 0\n" +
                        " 0 0 0.2353 -0.7586 1 0 0\n" +
                        " 0 0 0 0.2345 -0.7520 1 0\n" +
                        " 0 0 0 0 0.2320 -0.3750 1");
        DecimalVector y = Matrix.forwardElimination(b, L);
        assertArrayEquals(new double[]{4.0000, 9.0000, 16.7500, 13.5588, 7.3448, 4.3440, 3.9250}, y.toArray(), 0.001);
    }


    @Test
    public void Backwardsubstitution() {
        BandMatrix U = BandMatrix.parse(" 2 -2 1 0 0 0 0\n" +
                " 0 4 -3 1 0 0 0\n" +
                " 0 0 4.2500 -3.2500 1 0 0\n" +
                " 0 0 0 4.2647 -3.2353 1 0\n" +
                " 0 0 0 0 4.3103 -3.2414 1 \n" +
                " 0 0 0 0 0 3.3280 -1.2480\n" +
                " 0 0 0 0 0 0 1.3000");
        DecimalVector y = new DecimalVector(new double[]{4.0000, 9.0000, 16.7500, 13.5588, 7.3448, 4.3440, 3.9250});
        DecimalVector x = Matrix.backwardSubstitution(U, y);
        System.out.println(x);
        System.out.println(Arrays.toString(new double[]{4.788460, 6.245192, 6.913459, 4.759616, 2.836539, 2.437500, 3.019231}));
        assertArrayEquals(new double[]{4.788460, 6.245192, 6.913459, 4.759616, 2.836539, 2.437500, 3.019231}, x.toArray(), 0.0001);
    }

    @Test
    public void solvePenta() throws Exception {
        BandMatrix d = m(7);
        DecimalVector b = new DecimalVector(new double[]{
                4, 5, 12, 3, 1, 2, 4
        });
        DecimalVector r = d.solvePentadiagonale(b);
        assertArrayEquals(new double[]{4.788460, 6.245192, 6.913459, 4.759616, 2.836539, 2.437500, 3.019231}, r.toArray(), 0.00001);
    }

    @Test
    public void solveSOR() {
        BandMatrix d = m(7);
        DecimalVector b = new DecimalVector(new double[]{
                4, 5, 12, 3, 1, 2, 4
        });
        DecimalVector r = d.solveSOR(b);
        DecimalVector s = new DecimalVector(new double[]{4.788461, 6.245192, 6.913462, 4.759616, 2.836539, 2.437500, 3.019231});
        s.setPrecision(4);
        r.setPrecision(4);
        assertEquals(s, r);
        assertArrayEquals(s.toArray(), r.toArray(), 0.0001);
    }

    @Test
    public void mult() throws Exception {
        BandMatrix n = new BandMatrix(3, new int[]{0});
        // 1 0 0   1 1 1   1 1 1
        // 0 2 0 * 2 2 2 =  4 4 4
        // 0 0 3   3 3 3   9 9 9

        n.set(0, 0, 1);
        n.set(1, 1, 2);
        n.set(2, 2, 3);
        Matrix m = new DecimalMatrix(3);
        Matrix r = new DecimalMatrix(3);

        for (int i = 0; i < 3; i++) {
            m.set(0, i, 1);
            m.set(1, i, 2);
            m.set(2, i, 3);
            r.set(0, i, 1);
            r.set(1, i, 4);
            r.set(2, i, 9);
        }
        assertEquals(r.toString().replaceAll(".0", ""), n.mult(m).toString().replaceAll(".0", ""));

    }


    @Test
    public void add() throws Exception {
        BandMatrix n = new BandMatrix(3, new int[]{0});
        // 1 0 0   0 0 0   1 0 0
        // 0 2 0 + 0 0 0 =  0 2 0
        // 0 0 3   0 0 0   0 3 0

        n.set(0, 0, 1);
        n.set(1, 1, 2);
        n.set(2, 2, 3);
        BandMatrix m = new BandMatrix(3, new int[]{0});
        assertEquals(n.toString(), n.add(m).toString());
    }

    @Test
    public void multA() throws Exception {
        BandMatrix n = new BandMatrix(3, new int[]{0});
        // 1 0 0   1 1 1   1 1 1
        // 0 0 0 * 2 2 2 =  0 0 0
        // 0 0 0   3 3 3   0 0 0

        n.set(0, 0, 1);
        Matrix m = new DecimalMatrix(3);
        Matrix r = new DecimalMatrix(3);

        for (int i = 0; i < 3; i++) {
            m.set(0, i, 1);
            m.set(1, i, 2);
            m.set(2, i, 3);
            r.set(0, i, 1);
        }
        assertEquals(r.toString(), n.mult(m).toString());

    }

    @Test
    public void addA() throws Exception {
        // 1 0 0   1 1 1   2 1 1
        // 0 0 0 + 2 2 2 =  2 2 2
        // 0 0 1   3 3 3   3 3 4

        DecimalMatrix n = (DecimalMatrix) DecimalMatrix.parse("1 0 0\n" +
                "0 0 0\n" +
                "0 0 1");
        DecimalMatrix m = (DecimalMatrix) DecimalMatrix.parse("1 1 1\n" +
                "2 2 2\n" +
                "3 3 3");
        DecimalMatrix r = (DecimalMatrix) DecimalMatrix.parse("2 1 1\n" +
                "2 2 2\n" +
                "3 3 4");

        assertEquals(r.toString(), n.add(m).toString());

    }

    @Test
    public void transposeBasic() {
        BandMatrix m = m(4);
        assertEquals(m.toString(), m.transpose().transpose().toString());
    }


    @Test
    public void transpose2x4Value() {
        BandMatrix m = new BandMatrix(2, new int[]{0});
        m.set(1, 1, 4);
        assertEquals(4.0, m.transpose().get(1, 1), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void itShouldFailWhenWritingOutsideOfBand() {
        BandMatrix m = new BandMatrix(5, new int[]{0});
        m.set(0, 1, 5);
    }

    @Test
    public void itShouldNotFailInBand() {
        BandMatrix m = new BandMatrix(5, new int[]{0});
        m.set(0, 0, 5);
        assertEquals(5.0, m.get(0, 0));
    }

    @Test
    public void getPosition() {
        BandMatrix m = new BandMatrix(3, new int[]{-1, 0, 1});
        assertEquals(-1, m.getIndex(0, 2));
        assertEquals(-1, m.getIndex(2, 0));
        assertEquals(0, m.getIndex(1, 0));
        assertEquals(1, m.getIndex(2, 1));
        assertEquals(2, m.getIndex(0, 0));
        assertEquals(3, m.getIndex(1, 1));
        assertEquals(4, m.getIndex(2, 2));
        assertEquals(5, m.getIndex(0, 1));
        assertEquals(6, m.getIndex(1, 2));

    }

    @Test
    public void solveLargeMatrix() {
        BandMatrix m = BandMatrix.parse(
                "6 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0\n" +
                        "1 7 1 0 0 1 0 0 0 0 0 0 0 0 0 0\n" +
                        "0 1 7 1 0 0 1 0 0 0 0 0 0 0 0 0\n" +
                        "0 0 1 6 1 0 0 1 0 0 0 0 0 0 0 0\n" +
                        "1 0 0 1 7 1 0 0 1 0 0 0 0 0 0 0\n" +
                        "0 1 0 0 1 8 1 0 0 1 0 0 0 0 0 0\n" +
                        "0 0 1 0 0 1 8 1 0 0 1 0 0 0 0 0\n" +
                        "0 0 0 1 0 0 1 7 1 0 0 1 0 0 0 0\n" +
                        "0 0 0 0 1 0 0 1 7 1 0 0 1 0 0 0\n" +
                        "0 0 0 0 0 1 0 0 1 8 1 0 0 1 0 0\n" +
                        "0 0 0 0 0 0 1 0 0 1 8 1 0 0 1 0\n" +
                        "0 0 0 0 0 0 0 1 0 0 1 7 1 0 0 1\n" +
                        "0 0 0 0 0 0 0 0 1 0 0 1 6 1 0 0\n" +
                        "0 0 0 0 0 0 0 0 0 1 0 0 1 7 1 0\n" +
                        "0 0 0 0 0 0 0 0 0 0 1 0 0 1 7 1\n" +
                        "0 0 0 0 0 0 0 0 0 0 0 1 0 0 1 6\n"
        );


        DecimalVector b = new DecimalVector(new double[]{6, -8, 6, 4, 1, 8, 8, -8, 0, 4, -9, -5, -9, -4, 2, 6});
        DecimalVector r = new DecimalVector(new double[]{
                1.3274, -1.5957, 0.8061, 0.8341, -0.3690, 1.0363, 1.1192, -1.4414, 0.3849, 0.5547, -1.3547, -0.2482, -1.4388, -0.5040, 0.4123, 0.9727
        });


        System.out.println(m);
        System.out.println(b);
        DecimalVector res = m.solveSOR(b);
        assertArrayEquals(r.toArray(), res.toArray(), 0.0001);
    }


    @Test
    public void voidTestBandMult1() {
        BandMatrix B = BandMatrix.parse(" 1 0 0 0\n" +
                " 0 3 0 0\n" +
                " 0 0 4 0\n" +
                " 0 0 0 2");
        BandMatrix A = BandMatrix.parse(" 1 0 2 0\n" +
                " 0 3 0 -2\n" +
                " 0 0 5 0\n" +
                " 1 0 0 -1");

        BandMatrix A_B = BandMatrix.parse("1 0 8 0\n" +
                " 0 9 0 -4\n" +
                " 0 0 20 0\n" +
                " 1 0 0 -2");
        BandMatrix B_A = BandMatrix.parse(" 1 0 2 0\n" +
                " 0 9 0 -6\n" +
                " 0 0 20 0\n" +
                " 2 0 0 -2");
        assertTrue(B.mult(A) instanceof BandMatrix);
        assertTrue(A.mult(B) instanceof BandMatrix);
        assertEquals(A_B.toString(), A.mult(B).toString());
        assertEquals(B_A.toString(), B.mult(A).toString());
    }

    @Test
    public void ddt() {
        int size = 7;
        BandMatrix ddt = new BandMatrix(size, new int[]{-1, 0, 1});
        BandMatrix dtd = new BandMatrix(size, new int[]{-1, 0, 1});
        ddt.set(0, 0, 1);
        ddt.set(0, 1, -1);
        ddt.set(size - 1, size - 1, 2);
        ddt.set(size - 1, size - 2, -1);
        dtd.set(0, 0, 2);
        dtd.set(0, 1, -1);
        dtd.set(size - 1, size - 1, 1);
        dtd.set(size - 1, size - 2, -1);
        for (int row = 1; row < size - 1; row++) {
            ddt.set(row, row - 1, -1);
            ddt.set(row, row, 2);
            ddt.set(row, row + 1, -1);

            dtd.set(row, row - 1, -1);
            dtd.set(row, row, 2);
            dtd.set(row, row + 1, -1);
        }
        BandMatrix v = new BandMatrix(size, new int[]{0});
        for (int i = 0; i < v.rows(); i++) {
            v.set(i, i, i % 2 == 0 ? 1 : 0);
        }

        BandMatrix b = BandMatrix.parse("1 -2 1 0 0 0 0\n" +
                "-2 5 -4 1 0 0 0\n" +
                "1 -4 6 -4 1 0 0\n" +
                "0 1 -4 6 -4 1 0\n" +
                "0 0 1 -4 6 -4 1\n" +
                "0 0 0 1 -4 5 -2\n" +
                "0 0 0 0 1 -2 1");


        System.out.println("ddt = " + ddt);
        System.out.println("v = " + v);
        System.out.println("dtd = " + dtd);

        BandMatrix inner = (BandMatrix) v.mult(dtd);

        assertEquals(BandMatrix.parse("2 -1 0 0 0 0 0\n" +
                "0 0 0 0 0 0 0\n" +
                "0 -1 2 -1 0 0 0\n" +
                "0 0 0 0 0 0 0\n" +
                "0 0 0 -1 2 -1 0\n" +
                "0 0 0 0 0 0 0\n" +
                "0 0 0 0 0 -1 1"
        ).toString(), inner.toString());
        Matrix m = ddt.mult(inner);

        assertEquals(BandMatrix.parse(
                " 2 -1 0 0 0 0 0\n" +
                        " -2 2 -2 1 0 0 0\n" +
                        " 0 -2 4 -2 0 0 0\n" +
                        " 0 1 -2 2 -2 1 0\n" +
                        " 0 0 0 -2 4 -2 0\n" +
                        " 0 0 0 1 -2 2 -1\n" +
                        " 0 0 0 0 0 -2 2").toString(), m.toString());

        BandMatrix test = b.add(m);
        System.out.println(test.debug());
        assertEquals(BandMatrix.parse(
                " 3 -3 1 0 0 0 0\n" +
                        " -4 7 -6 2 0 0 0\n" +
                        " 1 -6 10 -6 1 0 0\n" +
                        " 0 2 -6 8 -6 2 0\n" +
                        " 0 0 1 -6 10 -6 1\n" +
                        " 0 0 0 2 -6 7 -3\n" +
                        " 0 0 0 0 1 -4 3").toString(), test.toString());

    }
}
