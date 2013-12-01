package View.ToneMappers;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 16.07.13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 * <p/>
 * <p/>
 * USES: http://stereopsis.com/gi93.pdf, Abs. 4. Spatial Uniform Mapping
 */
public class SpatiallyUniformMapping extends ToneMapping {

    private double treshold = -1;

    public SpatiallyUniformMapping() {

    }

    public SpatiallyUniformMapping(double T) {
        this.treshold = T;
    }

    @Override
    public double[][] getValuesIntern(double[][] e) {
        if (treshold == -1)
            treshold = calculateTreshold(e);
        double[][] r = new double[e.length][e[0].length];
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[i].length; j++) {
                if (e[i][j] < treshold) {
                    r[i][j] = (255.0 * e[i][j] / treshold);
                } else r[i][j] = 255;
            }
        }
        return r;
    }

    private double calculateTreshold(double[][] e) {
        double[] r = new double[e.length * e[0].length];
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {
                r[j * e.length + i] = e[i][j];
            }
        }
        Arrays.sort(r);
        double res = r[(int) (r.length * 0.8)];
        return res;

    }

    public String[] getVars() {
        return new String[]{"treshold"};
    }

    public String getVar(String s) {
        if (s.equals("treshold"))
            return this.treshold + "";
        else
            return null;
    }

    public boolean setVar(String var, String value) {
        try {
            if (var.equals("treshold")) {
                Double n = Double.parseDouble(value);
                if (!n.isNaN()) {
                    this.treshold = n.doubleValue();
                    return true;
                }
            }
        } catch (NumberFormatException e) {

        }

        return false;
    }
}
