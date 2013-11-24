package Maths;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 15.11.13
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class ArrayMaths {
    public static double[] intToDouble(int[] d) {
        double[] r = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            r[i] = d[i];
        }
        return r;
    }

    public static int[] TointArray(Integer[] indexes) {
        int[] r = new int[indexes.length];

        for (int i = 0; i < r.length; i++)
            r[i] = indexes[i].intValue();

        return r;
    }

    public static int[] TointArray(Set<Integer> indexes) {
        return TointArray((Integer[]) indexes.toArray(new Integer[indexes.size()]));
    }


    public static double round(double targetValue, int roundToDecimalPlaces) {

        int valueInTwoDecimalPlaces = (int) (targetValue * Math.pow(10, roundToDecimalPlaces));

        return (float) (valueInTwoDecimalPlaces / Math.pow(10, roundToDecimalPlaces));
    }


    /**
     * returns the maximum difference of the the arrays in each component
     *
     * @param a1
     * @param a2
     * @return maximum difference (regarded on components)
     */
    public static double diffMax(double[] a1, double[] a2) {
        if (a1.length != a2.length)
            throw new IllegalArgumentException("Arrays do not match");
        double max = 0;
        for (int i = 0; i < a1.length; i++) {
            max = Math.max(max, Math.abs(a1[i] - a2[i]));
        }
        return max;
    }
}
