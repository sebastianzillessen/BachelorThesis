package View.Plots;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 15.07.13
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class ImagePlot extends Plot {

    private final int height;
    private final int width;
    private Solver.Image image;
    BufferedImage bi = null;

    Thread generating;
    private Runnable runner = new Runnable() {
        @Override
        public void run() {
            bi = image.getBufferedImage();
        }
    };

    private void startImageCalculation() {
        if (generating == null || !generating.isAlive()) {
            generating = new Thread(runner);
            generating.start();
        }
    }

    public void setImage(Solver.Image image) {
        System.out.println("Set Image");
        this.image = image;
        startImageCalculation();
    }


    public ImagePlot(Solver.Image image) {
        super();
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        startImageCalculation();
    }

    @Override
    protected void redraw() {
        System.out.println("Redraw in Image Plot");
        bi = null;
        startImageCalculation();
        super.redraw();
    }

    @Override
    protected void paintPlot(Graphics g) {
        if (bi == null) {
            g.drawString("Image processing in Progress", 100, 100);
            if (bi == null) {
                System.out.println("Regenerate pircuter");
                this.startImageCalculation();
                try {
                    generating.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            g.clearRect(100, 100, 300, 50);
        }
        double w = getWidth() * 1.0 / image.getWidth();
        double h = getHeight() * 1.0 / image.getHeight();
        //if (w < 1 || h < 1) {
        double d = Math.min(Math.min(w, h), 1);
        g.drawImage(bi.getScaledInstance((int) (d * image.getWidth()), (int) (d * image.getHeight()), Image.SCALE_REPLICATE), 0, 0, null);
        /*} else {
            g.drawImage(bi, 0, 0, null);
        } */

    }


    @Override
    public void saveGraphic(String filename) {
        System.out.println("SaveGraphics in ImagePlot");
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(bi.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_REPLICATE), 0, 0, null);
        saveGraphic(filename, bufferedImage);
    }
}
