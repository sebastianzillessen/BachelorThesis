package View.ToneMappers;

import Maths.DefaultMatrix;
import Maths.Matrix;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 16.07.13
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 *
 * From: http://www.drama.uga.edu/~jkundert/DRAMA5310/SoundFX/SFX-Dvd1/FM%20Samples/12-03B/Drum%20Styles%20Vol4/ROCK_KIT/reinhard.pdf
 */
public class ReinhardMapping extends ToneMapping {

    private double a;

    public ReinhardMapping(double a) {
        this.a = a;
    }

    @Override
    public double[][] getValuesIntern(double[][] lum) {
        double[][] e = super.mapToRange(lum, 0, 1);
        int numPixels = e.length * e[0].length;
        double sum = 0;
        double delta = 0.000000001;
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {
                sum += Math.log(e[i][j] + delta);
            }
        }
        System.out.println(Math.exp(1.0 / numPixels));
        double key = Math.exp((1.0 / numPixels) * sum);
        Matrix m = new DefaultMatrix(e).mult(a / key);

        double[][] scaledLuminance = m.toArray();

        double[][] r = new double[e.length][e[0].length];
        int count = 0;
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[i].length; j++) {
                double v = scaledLuminance[i][j] / (scaledLuminance[i][j] + 1) * 255.0;

                if (v < 0 || v > 255) {
                    count++;
                    System.out.println("exited range: " + v + " " + count);
                }
                scaledLuminance[i][j] = v;
                r[i][j] = v;

            }
        }
        return r;
    }

    protected enum VARS {
        a
    }

    public String[] getVars() {
        String[] vars = new String[VARS.values().length];
        for (int i = 0; i < vars.length; i++)
            vars[i] = VARS.values()[i].toString();
        return vars;
    }

    public String getVar(String s) {
        switch (VARS.valueOf(s)) {
            case a:
                return this.a + "";
        }
        return null;
    }

    public boolean setVar(String var, String value) {
        System.out.println("SetVars in Reinhard");
        try {
            Double d = Double.valueOf(value);
            if (d.isNaN())
                return false;
            switch (VARS.valueOf(var)) {
                case a:
                    a = d.doubleValue();
                    break;
                default:
                    return false;
            }
            invalidateMapping();
            return true;
        } catch (NumberFormatException e) {

        }

        return false;
    }
}
