package Solver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: sebastianzillessen
 * Date: 27.10.13
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class Bitmap {
    private final int h;
    private final int w;
    protected BitSet data;

    public Bitmap(int w, int h) {
        this.w = w;
        this.h = h;
        data = new BitSet(w * h);
    }


    public int get(int x, int y) {
        return get(x + y * w);
    }

    private int get(int i) {
        return data.get(i) ? 1 : 0;
    }

    public void set(int x, int y, int value) {
        set(x + y * w, value);
    }

    public void set(int i, int value) {
        if (i >= 0 && i < data.size()) {
            if (value > 0)
                data.set(i);
            else
                data.clear(i);
        }
    }


    public void save(String filename) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img.getGraphics();
        for (int i = 0; i < data.length(); i++) {
            int b = data.get(i) ? 255 : 0;
            g.setColor(new Color(b, b, b));
            g.drawOval(i % w, i / w, 1, 1);
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


    public Bitmap shift(int xs, int ys) {
        Bitmap res = new Bitmap(w, h);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int i = (x + xs) + (y + ys) * w;
                if (x + xs >= 0 && x + xs < w && y + ys >= 0 && y + ys < h)
                    res.set(i, data.get(x + y * w) ? 1 : 0);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String s = "";
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                s += get(x, y) + " ";
            }
            s += "\n";
        }
        return s;
    }


    public Bitmap xor(Bitmap b) {
        Bitmap res = new Bitmap(w, h);
        res.data = data;
        res.data.xor(b.data);
        return res;
    }

    public Bitmap and(Bitmap b) {
        Bitmap res = new Bitmap(w, h);
        res.data = data;
        res.data.and(b.data);
        return res;
    }


    public int countBits() {
        return data.cardinality();
    }
}
