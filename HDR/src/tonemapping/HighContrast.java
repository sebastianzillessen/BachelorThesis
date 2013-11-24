package tonemapping;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 16.07.13
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class HighContrast extends ToneMapping {


    private double TM(double L) {
        return 255.0 * (C(L) - C(min)) / (C(max) - C(min));
    }

    private double C(double L) {
        if (L < 0.0034)
            return L / 0.0014;
        else if (L < 1)
            return 2.4483 + Math.log(L / 0.0034) / 0.4027;
        else if (L < 7.244)
            return 16.563 + (L - 1) / 0.4027;
        else
            return 32.0693 + Math.log(L / 7.2444) / 0.0556;
    }

    @Override
    public double[][] getValuesIntern(double[][] e) {

        double[][] r = new double[e.length][e[0].length];
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[i].length; j++) {
                r[i][j] = TM(e[i][j]);
            }
        }
        return r;
    }

}
