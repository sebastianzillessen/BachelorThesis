package View.Plots;

import Maths.ArrayMaths;
import Maths.Vector;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 02.07.13
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class ScatterPlot extends Plot {
    private boolean plotLine;
    private Scale scale;
    private int radius = 1;
    protected float zoomY = 1;
    protected float zoomX = 1;

    protected double minX;
    protected double maxX;
    protected double maxY;
    protected double minY;

    protected double[] x;
    protected double[] y;
    private String XDescription = "x";
    private String YDescription = "y";


    protected void setYZoom(float f) {
        if (f >= 0.1)
            this.zoomY = f;
        this.repaint();
    }

    protected void setXZoom(float f) {
        if (f >= 0.1)
            this.zoomX = f;
        this.repaint();
    }

    private double max(double[] x) {
        double m = x[0];
        for (double d : x) {
            m = Math.max(d, m);
        }

        return m;//((m / 5) + 1) * 5;
    }

    private double min(double[] x) {
        double m = 0;
        for (double d : x) {
            m = Math.min(d, m);
        }
        return m;//((m / 5) - 1) * 5;
    }


    public void setX(double[] x) {
        if (y == null) {
            throw new IllegalArgumentException("Set y first please!");
        } else if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must be of the same size (|x| = " + x.length + " |y| = " + y.length);
        }
        this.x = x;
        this.maxX = max(x);

        this.minX = Math.min(min(x), maxX);
        super.redraw();
    }


    public void setY(double[] y) {
        if (x == null) {
            this.y = y;
            double[] x = new double[y.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = i;
            }
            setX(x);
        } else if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must be of the same size (|x| = " + x.length + " |y| = " + y.length);
        }
        this.y = y;
        this.maxY = max(y);
        this.minY = Math.min(min(y), maxY);
        super.redraw();
    }

    public ScatterPlot(double[] x, double[] y) {
        super();
        setX(x);
        setY(y);
        this.scale = Scale.LINEAR;
        this.plotLine = true;
    }


    public ScatterPlot(double[] y, boolean plotLine) {
        this(y);
        this.plotLine = plotLine;
    }


    public ScatterPlot(double[] y) {
        super();
        setY(y);
        this.scale = Scale.LINEAR;
        this.plotLine = true;
    }

    public ScatterPlot(Vector y) {
        super();
        setY(y.toArray());
        this.scale = Scale.LINEAR;
        this.plotLine = true;
    }

    public ScatterPlot(double[] y, Scale scale) {
        this(y);
        if (minY <= 0 || minX <= 0)
            scale = Scale.LINEAR;
        else
            this.scale = scale;


    }


    public ScatterPlot(int[] histogram) {
        this(ArrayMaths.intToDouble(histogram));
    }

    @Override
    protected void paintPlot(Graphics g) {
        int w = g.getClipBounds().width;
        int h = g.getClipBounds().height;
        int d = 10;

        double scaleX, scaleY;
        Integer last_x = null, last_y = null;
        int arrow_size = 4;

        switch (scale) {
            case LINEAR:
                scaleX = (w * (d - 2) / d / (maxX - minX)) * zoomX;
                scaleY = (h * (d - 2) / d / (maxY - minY)) * zoomY;
                double xs = (maxX - minX) / (d - 2) / zoomX;
                double ys = (maxY - minY) / (d - 2) / zoomY;
                g.setColor(Color.LIGHT_GRAY);
                FontMetrics fm = g.getFontMetrics();
                for (int i = 1; i < d; i++) {
                    double valX = (minX + xs * (i - 1));
                    double valY = (minY + ys * (i - 1));
                    String xVal = String.format("%.1f", valX);
                    g.drawString(xVal, i * w / d - d / 2, h - h / d + fm.getHeight());
                    String yVal = String.format("%.1f", valY);
                    g.drawString(yVal, w / (d) - fm.stringWidth(yVal) - 10, h - i * h / d + fm.getHeight() / 2);

                    g.drawLine(i * w / d, h - h / (d + 1), i * w / d, h / d);
                    g.drawLine(w / (d + 1), h - i * h / d, w - w / d, h - i * h / d);
                }
                // draw 0 lines
                g.setColor(Color.black);
                int xZero = (int) Math.round(w / d - (minX * scaleX));
                int yZero = (int) Math.round(h - h / d + (minY * scaleY));
                // Zerolines
                g.drawLine(xZero, h - h / (d + 1), xZero, h / d);
                g.drawLine(w / (d + 1), yZero, w - w / d, yZero);
                //Ursprung
                g.drawOval(xZero - 3, yZero - 3, 6, 6);
                // arrows

                Polygon a1 = new Polygon();
                a1.addPoint(xZero + arrow_size, h / d);
                a1.addPoint(xZero, h / d - arrow_size);
                a1.addPoint(xZero - arrow_size, h / d);

                g.drawString(getYDescription(), xZero + 2 * arrow_size, h / d);
                g.drawString(getXDescription(), w - w / d + 2 * arrow_size, yZero);

                Polygon a2 = new Polygon();
                a2.addPoint(w - w / d, yZero - arrow_size);
                a2.addPoint(w - w / d + arrow_size, yZero);
                a2.addPoint(w - w / d, yZero + arrow_size);

                g.drawPolygon(a1);
                g.drawPolygon(a2);

                g.setColor(Color.RED);


                for (int i = 0; i < x.length; i++) {
                    if (x[i] != Double.NaN && y[i] != Double.NaN) {
                        int l = (int) (w / d + scaleX * x[i]);
                        int u = (int) (h - h / d - scaleY * (y[i] - minY));
                        g.drawOval(l - radius, u - radius, radius * 2, radius * 2);
                        if (last_x != null && last_y != null && plotLine)
                            g.drawLine(last_x, last_y, l, u);
                        last_x = l;
                        last_y = u;
                    }
                }
                break;
        }


    }

    @Override
    protected int getPlotWidth() {
        return getWidth();
    }

    @Override
    protected int getPlotHeight() {
        return getHeight();
    }

    public String getXDescription() {
        return XDescription;
    }

    public String getYDescription() {
        return YDescription;
    }

    public void setXDescription(String XDescription) {
        this.XDescription = XDescription;
    }

    public void setYDescription(String YDescription) {
        this.YDescription = YDescription;
    }
}
