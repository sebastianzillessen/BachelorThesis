package Maths;

/**
 * Created by sebastianzillessen on 30.11.13.
 */
public class EquotationSolverException extends Exception {
    public EquotationSolverException(String s) {
        super(s);
    }

    public EquotationSolverException(String s, AbstractMatrix m, Vector b) {
        this(String.format("%s : %n  %s %n  %s", s, m.debugString(), b.debugString()));
    }
}
