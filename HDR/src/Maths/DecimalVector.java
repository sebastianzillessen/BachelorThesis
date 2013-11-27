package Maths;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 12.07.13
 * Time: 15:25
 * To change this template use File | Settings | File Templates.
 */
public class DecimalVector {
    protected double[] v;
    private double max = -1;
    private double min = -1;
    private int precision = -1;

    public DecimalVector(int n) {
        v = new double[n];
        for (int j = 0; j < v.length; j++) {
            v[j] = 0;
        }
    }

    public DecimalVector(double[] values) {
        this(values.length);
        for (int i = 0; i < values.length; i++) {
            set(i, values[i]);
        }
    }

    public DecimalVector(int[] values) {
        this(values.length);
        for (int i = 0; i < values.length; i++) {
            set(i, values[i]);
        }
    }

    public DecimalVector add(DecimalVector v) {
        if (v.length() == length()) {
            DecimalVector r = new DecimalVector(length());
            for (int i = 0; i < length(); i++) {
                r.set(i, get(i) + (v.get(i)));
            }
            return r;
        } else
            throw new IllegalArgumentException("Wrong length");
    }

    public DecimalVector subtract(double v) {
        return add(-v);
    }


    public DecimalVector subtract(DecimalVector v) {
        if (v.length() == length()) {
            DecimalVector r = new DecimalVector(length());
            for (int i = 0; i < length(); i++) {
                r.set(i, get(i) - v.get(i));
            }
            return r;
        } else
            throw new IllegalArgumentException("Wrong length self: " + length() + " other: " + v.length());
    }

    public double abs2() {
        double t = 0;
        for (int i = 0; i < length(); i++) {
            t += v[i] * v[i];
        }
        return t;
    }


    public DecimalVector add(double d) {
        DecimalVector r = new DecimalVector(length());
        for (int i = 0; i < length(); i++) {
            r.set(i, v[i] + d);
        }
        return r;
    }

    public DecimalVector exp() {
        DecimalVector v = new DecimalVector(length());
        for (int i = 0; i < v.length(); i++) {
            v.set(i, Math.exp(get(i)));
        }
        return v;
    }

    public DecimalVector add(BigDecimal d) {
        return this.add(d.doubleValue());
    }

    public void set(int i, BigDecimal f) {
        set(i, f.doubleValue());
    }

    public void set(int i, double f) {
        if (Double.isNaN(f) || Double.isInfinite(f)) {
            throw new ArithmeticException("Value is " + f);
        }
        v[i] = f;
        min = -1;
        max = -1;
    }

    public double get(int i) {
        return v[i];
    }

    public int length() {
        return v.length;
    }

    public DecimalVector copy() {
        DecimalVector res = new DecimalVector(length());
        for (int i = 0; i < length(); i++)
            res.set(i, get(i));
        return res;
    }


    public String toString() {
        String s = "[";
        for (int i = 0; i < length(); i++) {
            s += precision >= 0 ? ArrayMaths.round(get(i), precision) : get(i);
            if (i < length() - 1)
                s += " ";
        }
        s += "]";
        return s;
    }

    public double[] toArray() {
        double[] r = new double[length()];
        for (int i = 0; i < length(); i++)
            r[i] = get(i);
        return r;
    }


    public double absMax() {
        double absMax = Math.abs(get(0));
        for (int i = 1; i < length(); i++) {
            absMax = Math.max(absMax, Math.abs(v[i]));
        }
        return absMax;
    }

    public double max() {
        if (this.max == -1) {
            this.max = get(0);
            for (int i = 1; i < length(); i++) {
                max = Math.max(max, v[i]);
            }
        }
        return max;
    }

    public double min() {
        if (this.min == -1) {
            this.min = get(0);
            for (int i = 1; i < length(); i++) {
                min = Math.min(min, v[i]);
            }
        }
        return min;
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DecimalVector))
            return false;
        DecimalVector d = (DecimalVector) other;
        if (d.length() != length())
            return false;
        int p = Math.max(d.precision, precision);
        for (int i = 0; i < length(); i++)
            if (get(i) - d.get(i) > Math.pow(10, precision))
                return false;
        return true;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getPrecision() {
        return precision;
    }


    public void setRandom(double percentage, double lower, double upper) {
        int set = 0;
        if (percentage < 0 || percentage > 1) {
            throw new IllegalArgumentException("Percentage should be between 0 and 1");
        }
        if (lower >= upper) {
            throw new IllegalArgumentException("Lower must be lower then upper!");
        }
        while (set * 1.0 / (length()) < percentage) {
            int c = (int) Math.floor(Math.random() * length());
            double v = Math.random() * (upper - lower) + lower;
            try {
                if (get(c) == 0) {
                    set(c, v);
                    set++;
                }
            } catch (IndexOutOfBoundsException e) {

            }
        }
    }

    public void toFile(final String filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(filename));
                    writer.write(DecimalVector.this.toString());
                    System.out.println("File '" + filename + "' saved successfully.");
                } catch (IOException e) {
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).start();
    }
}
