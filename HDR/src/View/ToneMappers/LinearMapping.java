package View.ToneMappers;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 10.11.13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class LinearMapping extends ToneMapping {
    @Override
    public double[][] getValuesIntern(double[][] e) {
        return e;
    }
}
