package tonemapping;

import Solver.Image;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 16.07.13
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public abstract class ToneMapping {
    private Image image;

    protected ToneMapping() {

    }

    protected void invalidateMapping() {
        this.image = null;
        System.out.println("invalidate image");
    }

    public short[][] getValues(double[][] e) {
        System.out.println("GETVALUES");
        minMax(e);
        e = mapToRange(e, 0, 1);
        minMax(e);
        return mapToShort(getValuesIntern(e), 0, 255);
    }

    protected abstract double[][] getValuesIntern(double[][] e);

    protected double max;
    protected double min;

    protected void minMax(double[][] e) {
        min = min(e);
        max = max(e);
    }

    protected double max(double[][] e) {
        double v = e[0][0];
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {
                v = Math.max(e[i][j], v);
            }
        }
        return v;
    }

    protected double min(double[][] e) {
        double v = e[0][0];
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[i].length; j++) {
                v = Math.min(e[i][j], v);
            }
        }
        return v;
    }

    protected double mapToRange(double val) {
        return mapToRange(val, min, max, 0, 255);
    }

    protected double mapToRange(double val, double in_lower, double in_upper, double out_lower, double out_upper) {
        double out_range = out_upper - out_lower;
        double in_range = in_upper - in_lower;
        double in_val = val - in_lower;
        val = (in_val / in_range) * out_range;
        return out_lower + val;
    }

    public double[][] mapToRange(double[][] luminanceMap, double lower, double upper) {
        double max = max(luminanceMap);
        double min = min(luminanceMap);
        double[][] res = new double[luminanceMap.length][luminanceMap[0].length];
        for (int i = 0; i < luminanceMap.length; i++) {
            for (int j = 0; j < luminanceMap[0].length; j++) {
                res[i][j] = mapToRange(luminanceMap[i][j], min, max, lower, upper);
            }
        }
        return res;

    }

    public short[][] mapToShort(double[][] luminanceMap, double lower, double upper) {
        double[][] d = mapToRange(luminanceMap, lower, upper);
        short[][] res = new short[luminanceMap.length][luminanceMap[0].length];
        for (int i = 0; i < luminanceMap.length; i++) {
            for (int j = 0; j < luminanceMap[0].length; j++) {
                res[i][j] = (short) Math.round(d[i][j]);
            }
        }
        return res;
    }

    public String[] getVars() {
        return new String[]{};
    }

    public String getVar(String s) {
        return null;
    }

    public boolean setVar(String var, String value) {
        return true;
    }

    public Image getImage(int width, int height, double[] doubles) {
        System.out.println("GetImage");
        if (image == null) {
            System.out.println("=> NEW IMAGE");
            image = new Image(width, height);
            double[][] d = new double[width][height];
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    d[w][h] = doubles[w + h * width];
                }
            }
            short[][] res = getValues(d);
            for (int x = 0; x < res.length; x++) {
                for (int y = 0; y < res[x].length; y++) {
                    image.set(x, y, res[x][y]);
                }
            }
        }
        return image;
    }
}
