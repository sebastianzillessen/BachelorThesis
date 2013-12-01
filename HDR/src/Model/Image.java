package Model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Image {
    protected String fileName = "";
    protected double exposureTime = -1;
    protected BufferedImage grayscale;
    protected int[] data;
    protected int[] histogram = new int[256];
    protected int w;
    protected int h;

    private int min = -1;
    private int max = -1;


    public Image(int w, int h) {
        this.w = w;
        this.h = h;
        this.data = new int[w * h];
    }


    public Image(String fileName, double exposureTime) throws Exception {
        this.fileName = fileName;
        this.exposureTime = exposureTime;
        if (!readFile()) {
            throw new Exception("File not found");
        }
    }


    private boolean readFile() {
        try {
            BufferedImage img = ImageIO.read(new File(this.fileName));
            w = img.getWidth();
            h = img.getHeight();
            grayscale = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayscale.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            System.out.println(grayscale.getRGB(0, 0));
            System.out.println(grayscale.getRGB(0, 1));
            System.out.println(grayscale.getRGB(1, 0));
            System.out.println(grayscale.getRGB(1, 1));
            readLuminance();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean save(String filename) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int c = data[x + y * w];
                img.setRGB(x, y, new Color(c, c, c).getRGB());
            }
        }
        if (!filename.endsWith(".png"))
            filename += ".png";
        try {
            ImageIO.write(img, "png", new File(filename));
            return true;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
    }

    private void readLuminance() {
        data = new int[w * h];
        byte[] pixels = (byte[]) grayscale.getData().getDataElements(0, 0, w,
                h, null);
        for (int i = 0; i < w * h; i++) {
            data[i] = (int) (pixels[i] & 0xff);
        }
        updateHistogram();
    }


    public int[] getHistogram() {
        updateHistogram();
        return this.histogram;
    }

    public double getExposureTime() {
        return exposureTime;
    }

    public int getValue(int i) {
        if (data != null && i >= 0 && i < w * h) {
            return data[i];
        } else {
            return -1;
        }
    }

    public String toString() {
        return String.format("Picture '%s' (%4d,%4d) t: %.6fs Max: %3d Min: %3d Median: %3.0f", fileName, w, h, exposureTime, max, min, getMedian());
    }

    public double getMedian() {
        int[] sortedArray = Arrays.copyOf(data, data.length);
        Arrays.sort(sortedArray);
        System.out.println(Arrays.toString(sortedArray));
        double median;
        if (sortedArray.length % 2 == 0)
            median = ((double) sortedArray[sortedArray.length / 2 - 1] + (double) sortedArray[sortedArray.length / 2]) / 2;
        else
            median = (double) sortedArray[sortedArray.length / 2];
        return median;
    }

    public int getImageSize() {
        return data.length;
    }

    public int getHeight() {
        return h;
    }

    public int getWidth() {
        return w;
    }


    public Image downsample() {
        if (this.w > 1 && this.h > 1) {
            Image img = new Image(this.w / 2, this.h / 2);
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    img.set(i, j, get(i * 2, j * 2));
                }
            }
            img.updateHistogram();
            return img;
        }
        return this.copy();

    }

    public Image copy() {
        Image r = new Image(w, h);
        r.data = this.data.clone();
        r.updateHistogram();
        return r;
    }

    public void set(int x, int y, int value) {
        data[x + y * w] = value;
    }

    public int get(int x, int y) {
        return getValue(x + y * w);
    }

    private void updateHistogram() {
        histogram = new int[256];
        if (data.length > 0) {
            max = data[0];
            min = data[0];
            for (int i = 0; i < data.length; i++) {
                histogram[data[i]] += 1;
                max = (int) Math.max(data[i], max);
                min = (int) Math.min(data[i], min);
            }
        }
    }


    public Image shiftedInstance(int xs, int ys) {
        Image res = new Image(w, h);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                res.set(x, y, 0);
            }
        }
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                if (x + xs >= 0 && x + xs < w && y + ys >= 0 && y + ys < h) {
                    res.set(x + xs, y + ys, get(x, y));
                }
            }
        }
        res.updateHistogram();
        return res;
    }

    public void addSaltAndPepper(double percentage) {
        for (int i = 0; i < data.length; i++) {
            // should we add nois?
            if (Math.random() <= percentage) {
                if (Math.random() >= .5)
                    data[i] = 255;
                else
                    data[i] = 0;
            }
        }
        updateHistogram();
    }

    public BufferedImage getBufferedImage() {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                short c = (short) get(x, y);
                bi.setRGB(x, y, new Color(c, c, c).getRGB());
            }
        }
        return bi;
    }

    public void addGaussian(double devStd) {
        Random r = new Random();
        for (int i = 0; i < data.length; i++) {
            int src = data[i];
            int c = (int) (src + (devStd * (r.nextGaussian())));
            if (c < 0)
                c = 0;
            if (c > 255)
                c = 255;
            data[i] = c;
        }
        updateHistogram();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || o instanceof Image) {
            Image i = (Image) o;
            if (i.getWidth() != getWidth() || i.getHeight() != getHeight() || i.getExposureTime() != getExposureTime())
                return false;
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    if (i.get(x, y) != get(x, y))
                        return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}