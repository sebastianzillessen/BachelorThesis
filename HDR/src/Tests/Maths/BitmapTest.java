package Tests.Maths;

import Maths.DecimalMatrix;
import Maths.DecimalVector;
import Solver.Bitmap;
import org.junit.Before;
import org.junit.Test;
import sun.java2d.loops.XORComposite;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 13.07.13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class BitmapTest {
    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testToString() {
        Bitmap b = new Bitmap(3, 3);
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.toString());
    }

    @Test
    public void testSet() {
        Bitmap b = new Bitmap(3, 3);
        b.set(1, 1, 1);
        assertEquals("0 0 0 \n0 1 0 \n0 0 0 \n", b.toString());
        b.set(0, 1, 1);
        assertEquals("0 0 0 \n1 1 0 \n0 0 0 \n", b.toString());
    }

    @Test
    public void testShiftDown() {
        Bitmap b = new Bitmap(3, 3);
        // second row ist set
        b.set(0, 1, 1);
        b.set(1, 1, 1);
        b.set(2, 1, 1);
        assertEquals("0 0 0 \n0 0 0 \n1 1 1 \n", b.shift(0, 1).toString());
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.shift(0, 2).toString());
    }

    @Test
    public void testShiftUp() {
        Bitmap b = new Bitmap(3, 3);
        // second row ist set
        b.set(0, 1, 1);
        b.set(1, 1, 1);
        b.set(2, 1, 1);
        assertEquals("1 1 1 \n0 0 0 \n0 0 0 \n", b.shift(0, -1).toString());
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.shift(0, -2).toString());
    }

    @Test
    public void testShiftLeft() {
        Bitmap b = new Bitmap(3, 3);
        // second row ist set
        b.set(1, 0, 1);
        b.set(1, 1, 1);
        b.set(1, 2, 1);
        assertEquals("1 0 0 \n1 0 0 \n1 0 0 \n", b.shift(-1, 0).toString());
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.shift(-2, 0).toString());
    }

    @Test
    public void testShiftRight() {
        Bitmap b = new Bitmap(3, 3);
        // second row ist set
        b.set(1, 0, 1);
        b.set(1, 1, 1);
        b.set(1, 2, 1);
        System.out.println(b);
        System.out.println(b.shift(0, 0));
        System.out.println(b.shift(1, 0));
        System.out.println(b.shift(2, 0));
        assertEquals("0 0 1 \n0 0 1 \n0 0 1 \n", b.shift(1, 0).toString());
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.shift(2, 0).toString());
    }

    @Test
    public void testShiftUpRight() {
        Bitmap b = new Bitmap(3, 3);
        b.set(1, 1, 1);
        assertEquals("0 0 1 \n0 0 0 \n0 0 0 \n", b.shift(1, -1).toString());
        assertEquals("0 0 0 \n0 0 1 \n0 0 0 \n", b.shift(1, 0).toString());
        assertEquals("0 0 0 \n0 0 1 \n0 0 0 \n", b.shift(1, -1).shift(0, 1).toString());
        assertEquals("0 0 0 \n0 0 0 \n0 0 0 \n", b.shift(2, -2).toString());
    }

    @Test
    public void testXor() {
        Bitmap b1 = new Bitmap(2, 2);
        b1.set(0, 1, 1);
        b1.set(1, 1, 1);
        // 0 0
        // 1 1
        Bitmap b2 = new Bitmap(2, 2);
        b2.set(0, 0, 1);
        b2.set(0, 1, 1);
        // 1 0
        // 1 0
        assertEquals("1 0 \n0 1 \n", b1.xor(b2).toString());
    }

    @Test
    public void testShift() {
        Bitmap b = new Bitmap(1, 1);
        b.set(0, 0, 1);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    assertEquals(0, b.shift(i, j).get(0, 0));
                }
            }
        }
    }

    @Test
    public void testAnd() {
        Bitmap b1 = new Bitmap(2, 2);
        b1.set(0, 1, 1);
        b1.set(1, 1, 1);
        // 0 0
        // 1 1
        Bitmap b2 = new Bitmap(2, 2);
        b2.set(0, 0, 1);
        b2.set(0, 1, 1);
        // 1 0
        // 1 0
        assertEquals("0 0 \n1 0 \n", b1.and(b2).toString());
    }
}