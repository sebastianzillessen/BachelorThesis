package Model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Image {
    protected String fileName;
    protected float exposureTime;
    protected BufferedImage grayscale;
    protected int[] data;
    protected int[] histogram;
    protected int w;
    protected int h;

    private int min = -1;
    private int max = -1;


    public Image(int w, int h) {
        this.w = w;
        this.h = h;
        this.data = new int[w * h];
    }


    public Image(String fileName, float exposureTime) throws Exception {
        this.fileName = fileName;
        this.exposureTime = exposureTime;
        this.histogram = new int[256];
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
            readLuminance();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void save(String filename) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img.getGraphics();
        for (int i = 0; i < data.length; i++) {
            g.setColor(new Color(data[i], data[i], data[i]));
            g.drawOval(i % getWidth(), i / getWidth(), 1, 1);
        }
        g.dispose();
        if (!filename.endsWith(".png"))
            filename += ".png";
        try {
            ImageIO.write(img, "png", new File(filename));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void readLuminance() {
        data = new int[w * h];
        byte[] pixels = (byte[]) grayscale.getData().getDataElements(0, 0, w,
                h, null);
        for (int i = 0; i < w * h; i++) {
            data[i] = (int) (pixels[i] & 0xff);
        }
        updateHistogram();
    }


    public int[] getHistogram() {
        return this.histogram;
    }

    public float getExposureTime() {
        return exposureTime;
    }

    public int getValue(int i) {
        if (data != null && i >= 0 && i < w * h) {
            return data[i];
        } else {
            System.out.println("wrong parameter for #getValue: " + i);
            return -1;
        }
    }

    public String toString() {
        return String.format("Picture '%s' (%4d,%4d) t: %.6fs Max: %3d Min: %3d Median: %3d", fileName, w, h, exposureTime, max, min, getMedian());
    }

    public int getMedian() {
        int m[] = Arrays.copyOf(histogram, histogram.length);
        int l = 0;
        int h = m.length - 1;
        while (l != h) {
            if (m[l] > m[h]) {
                m[l] -= m[h];
                m[h] = 0;
                h--;
            } else {
                m[h] -= m[l];
                m[l] = 0;
                l++;
            }
        }
        return l;
    }

    public void printHistogram() {
        String s = "";
        for (int i = 0; i <= 25; i++) {
            for (int j = 0; j < 10 && i * 10 + j < 256; j++) {
                s += String.format("   %3d : %6d  | ", i * 10 + j, histogram[i * 10 + j]);
            }
            s += String.format("%n");

        }
        System.out.println(s);
    }

    public int getImageSize() {
        return data.length;
    }

    public String getValues() {
        String s = "";
        for (int i = 0; i < data.length; i++) {
            s += data[i] + " ";
        }
        return s;
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
            if (Math.random() > 1.0 - percentage) {
                data[i] = 255;
            } else if (Math.random() < percentage) {
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
}